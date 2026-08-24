package com.timeline.service

import android.Manifest
import android.R
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.Session
import com.timeline.domain.UserPreferences
import com.timeline.worker.ScreenshotWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

/**
 * CHANGES vs. original (this conversation):
 *
 * 1. onTaskRemoved() override — added. Without it, START_STICKY alone gives
 *    no control over restart timing, so a "Clear All" sweep could leave the
 *    foreground notification and tracking down for an unpredictable stretch.
 *    Restart is scheduled via AlarmManager at a 1s delay rather than calling
 *    startForegroundService() directly from onTaskRemoved(), since a direct
 *    call is sometimes dropped by the system mid-teardown.
 *
 *    IMPORTANT (see conversation): this restarts the *process* only. It does
 *    NOT and cannot restore TimelineAccessibilityService's binding — that is
 *    a Settings-level OS toggle, not process state, and no code on this side
 *    of that boundary can flip it back on. Restoring accessibility always
 *    requires a real human tap in system Settings.
 *
 * 2. checkAccessibilityStateChange() — added, called once per pollUsageStats()
 *    cycle. TimelineAccessibilityService.getInstance() returning null used to
 *    fail silently into generateFallbackSnapshot() with only a log line, so a
 *    revoked-accessibility state could go unnoticed while blank placeholder
 *    images kept getting saved as if they were real captures. This detects
 *    the true→false transition and posts a visible, tappable notification
 *    instead of degrading silently.
 *
 * 3. Screenshot capture — restored to go through ScreenshotWorker via
 *    WorkManager (enqueueScreenshot()) instead of the inline
 *    captureAndSaveScreenshot() duplicate that had crept back in. The
 *    capture/save/session-update logic now lives only in ScreenshotWorker;
 *    this service just enqueues the work. The request is marked
 *    setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST) so it runs promptly
 *    while the app has quota, and silently degrades to a regular deferrable
 *    job (subject to Doze/App Standby delay) once quota is exhausted,
 *    rather than throwing.
 *
 * Everything else (poll loop, session logic) is reproduced unchanged from
 * the file as shared in this conversation.
 */
class TrackingService : Service() {

    private val repository: TimelineRepository by inject()
    private val exclusionPolicy: ExclusionPolicy by inject()
    private val userPreferences: UserPreferences by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var trackingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    private var excludedPackages = setOf<String>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        serviceScope.launch {
            exclusionPolicy.getExcludedPackages().collect {
                excludedPackages = it
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d { "TrackingService started" }
        startForeground(NOTIFICATION_ID, buildNotification())
        startTracking()

        return START_STICKY
    }

    /**
     * Fires for both a manual single-app swipe and a full "Clear All" sweep —
     * Android does not distinguish them at this callback. Schedules a fast
     * restart via AlarmManager so the foreground notification and tracking
     * loop come back within ~1s, matching the behavior observed from apps
     * like WhatsApp/Play Store during the recorded Clear All analysis.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        Logger.d { "onTaskRemoved fired - restarting TrackingService" }
        val restartIntent = Intent(applicationContext, TrackingService::class.java).apply {
            setPackage(packageName)
        }
        val restartPendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartPendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Everett Tracking Active")
            .setContentText("Recording activity journal...")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (isActive) {
                pollUsageStats()
                delay(5000.milliseconds) // Adaptive polling: 5s interval
            }
        }
    }

    private var lastApp: String? = null
    private var lastStartTime: Long = 0
    private var currentSessionId: String? = null
    private var lastScreenshotTime: Long = 0
    private var wasAccessibilityGranted = true

    private suspend fun pollUsageStats() {
        val prefs = userPreferences.state.first()
        if (!prefs.isUsageTrackingEnabled) {
            // If tracking is disabled, close any active session
            currentSessionId?.let { closePreviousSession(it) }
            currentSessionId = null
            lastApp = null
            return
        }

        checkAccessibilityStateChange()

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // Polling every 5s, so 10s lookback is enough and more efficient

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
                Logger.withTag("TrackingService").v { "Detected RESUMED event for: $currentApp" }
            }
        }

        if (currentApp != null) {
            if (currentApp != lastApp) {
                Logger.withTag("TrackingService").d { "App transition detected: $lastApp -> $currentApp" }

                // Close previous session if it exists
                currentSessionId?.let { sessionId ->
                    closePreviousSession(sessionId)
                }

                val isExcluded = exclusionPolicy.isExcluded(currentApp)
                if (isExcluded) {
                    Logger.withTag("TrackingService").d { "App $currentApp is excluded, skipping" }
                    lastApp = currentApp
                    currentSessionId = null
                    return
                }

                Logger.withTag("TrackingService").i { "Recording session for: $currentApp" }
                val sessionId = UUID.randomUUID().toString()
                currentSessionId = sessionId
                lastApp = currentApp
                lastStartTime = System.currentTimeMillis()
                lastScreenshotTime = lastStartTime

                startNewSession(sessionId, currentApp)
            } else {
                // Periodic screenshot check (every 1 minute)
                if (currentSessionId != null && (System.currentTimeMillis() - lastScreenshotTime) >= 60000L) {
                    Logger.withTag("TrackingService").d { "Periodic screenshot trigger for $currentApp" }
                    lastScreenshotTime = System.currentTimeMillis()
                    enqueueScreenshot(currentSessionId!!, currentApp)
                }
            }
        }
    }

    /**
     * Detects the true->false transition on TimelineAccessibilityService's
     * connection state and surfaces it via notification instead of letting
     * ScreenshotWorker silently fall back to placeholder bitmaps on every
     * capture. Does NOT attempt to restore accessibility itself — that
     * requires a human tap in system Settings and cannot be done from
     * process code.
     */
    private fun checkAccessibilityStateChange() {
        val isGranted = TimelineAccessibilityService.getInstance() != null
        if (wasAccessibilityGranted && !isGranted) {
            Logger.withTag("TrackingService").w { "Accessibility service disconnected — likely revoked by Clear All or Force Stop" }
            showAccessibilityLostNotification()
        }
        wasAccessibilityGranted = isGranted
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showAccessibilityLostNotification() {
        val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            this, 2, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screenshot capture stopped")
            .setContentText("Tap to re-enable accessibility permission")
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_ACCESSIBILITY_LOST, notification)
    }

    private fun closePreviousSession(sessionId: String) {
        serviceScope.launch {
            try {
                val session = repository.getSession(sessionId)
                if (session != null) {
                    val endTime = kotlin.time.Clock.System.now()
                    val duration = (endTime - session.startTime).inWholeMinutes
                    val updatedSession = session.copy(
                        endTime = endTime,
                        durationMinutes = duration
                    )
                    repository.saveSession(updatedSession)
                    Logger.i(tag = "TrackingService") { "Closed session $sessionId. Duration: ${duration}m" }
                }
            } catch (e: Exception) {
                Logger.e(e, "TrackingService") { "Failed to close session $sessionId" }
            }
        }
    }

    private fun startNewSession(sessionId: String, packageName: String) {
        serviceScope.launch {
            try {
                val session = Session(
                    id = sessionId,
                    packageName = packageName,
                    startTime = kotlin.time.Clock.System.now(),
                    endTime = null,
                    durationMinutes = 0,
                    screenshots = emptyList(),
                    segments = emptyList()
                )
                repository.saveSession(session)
                Logger.i(tag = "TrackingService") { "Successfully saved session for $packageName" }
                enqueueScreenshot(sessionId, packageName)
            } catch (e: Exception) {
                Logger.e(e, "TrackingService") { "Failed to start session for $packageName" }
            }
        }
    }

    private fun enqueueScreenshot(sessionId: String, packageName: String) {
        val workRequest = OneTimeWorkRequestBuilder<ScreenshotWorker>()
            .setInputData(workDataOf(
                "package_name" to packageName,
                "session_id" to sessionId
            ))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
        Logger.d(tag = "TrackingService") { "Enqueued ScreenshotWorker for $packageName (Session: $sessionId)" }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timeline Tracking",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_ID_ACCESSIBILITY_LOST = 1002
        private const val CHANNEL_ID = "tracking_channel"
    }
}