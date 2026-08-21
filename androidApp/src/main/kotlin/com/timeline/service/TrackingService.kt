package com.timeline.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

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
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Everett Tracking Active")
            .setContentText("Recording activity journal...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startTracking()

        return START_STICKY
    }

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

    private suspend fun pollUsageStats() {
        val prefs = userPreferences.state.first()
        if (!prefs.isUsageTrackingEnabled) {
            // If tracking is disabled, close any active session
            currentSessionId?.let { closePreviousSession(it) }
            currentSessionId = null
            lastApp = null
            return
        }

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // Polling every 5s, so 10s lookback is enough and more efficient

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
                Logger.v("TrackingService") { "Detected RESUMED event for: $currentApp" }
            }
        }

        if (currentApp != null) {
            if (currentApp != lastApp) {
                Logger.d("TrackingService") { "App transition detected: $lastApp -> $currentApp" }
                
                // Close previous session if it exists
                currentSessionId?.let { sessionId ->
                    closePreviousSession(sessionId)
                }

                val isExcluded = exclusionPolicy.isExcluded(currentApp)
                if (isExcluded) {
                    Logger.d("TrackingService") { "App $currentApp is excluded, skipping" }
                    lastApp = currentApp
                    currentSessionId = null
                    return
                }

                Logger.i("TrackingService") { "Recording session for: $currentApp" }
                val sessionId = java.util.UUID.randomUUID().toString()
                currentSessionId = sessionId
                lastApp = currentApp
                lastStartTime = System.currentTimeMillis()
                lastScreenshotTime = lastStartTime
                
                startNewSession(sessionId, currentApp)
            } else {
                // Periodic screenshot check (every 1 minute)
                if (currentSessionId != null && (System.currentTimeMillis() - lastScreenshotTime) >= 60000L) {
                    Logger.d("TrackingService") { "Periodic screenshot trigger for $currentApp" }
                    lastScreenshotTime = System.currentTimeMillis()
                    enqueueScreenshot(currentSessionId!!, currentApp)
                }
            }
        }
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
                    Logger.i("TrackingService") { "Closed session $sessionId. Duration: ${duration}m" }
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
                Logger.i("TrackingService") { "Successfully saved session for $packageName" }
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
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
        Logger.d("TrackingService") { "Enqueued ScreenshotWorker for $packageName (Session: $sessionId)" }
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
        private const val CHANNEL_ID = "tracking_channel"
    }
}
