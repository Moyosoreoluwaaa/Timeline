package com.timeline.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.IBinder
import androidx.core.app.NotificationCompat
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.Session
import com.timeline.domain.SessionSegment
import com.timeline.domain.UserPreferences
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
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
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
                    captureAndSaveScreenshot(currentSessionId!!, currentApp)
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
                captureAndSaveScreenshot(sessionId, packageName)
            } catch (e: Exception) {
                Logger.e(e, "TrackingService") { "Failed to start session for $packageName" }
            }
        }
    }

    private fun captureAndSaveScreenshot(sessionId: String, packageName: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val prefs = userPreferences.state.first()
                if (!prefs.isScreenshotCaptureEnabled) {
                    Logger.d("TrackingService") { "Screenshot capture disabled in settings, skipping for $packageName" }
                    return@launch
                }

                val accessibilityService = TimelineAccessibilityService.getInstance()
                val bitmap = if (accessibilityService != null) {
                    accessibilityService.captureScreenshot()
                } else {
                    Logger.withTag("TrackingService").w { "AccessibilityService not connected. Using fallback snapshot for $packageName." }
                    generateFallbackSnapshot(packageName)
                }

                if (bitmap != null) {
                    val screenshotPath = saveBitmap(bitmap, packageName)
                    if (screenshotPath != null) {
                        updateSessionWithScreenshot(sessionId, screenshotPath)
                    }
                }
            } catch (e: Exception) {
                Logger.e(e, "TrackingService") { "Failed to capture/save screenshot for $packageName" }
            }
        }
    }

    private fun generateFallbackSnapshot(packageName: String): Bitmap {
        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        paint.color = 0xFF121212.toInt()
        canvas.drawRect(0f, 0f, 720f, 1280f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Snapshot of $packageName", 360f, 600f, paint)
        canvas.drawText(
            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date()),
            360f, 680f, paint
        )
        
        return bitmap
    }

    private fun saveBitmap(bitmap: Bitmap, packageName: String): String? {
        val filename = "screenshot_${packageName}_${System.currentTimeMillis()}.png"
        val file = File(filesDir, "screenshots").apply { mkdirs() }
        val targetFile = File(file, filename)
        
        return try {
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            Logger.e(e, "TrackingService") { "Failed to save screenshot" }
            null
        }
    }

    private suspend fun updateSessionWithScreenshot(sessionId: String, screenshotPath: String) {
        val session = repository.getSession(sessionId)
        if (session != null) {
            val newSegment = SessionSegment(
                timestamp = kotlin.time.Clock.System.now(),
                screenshotPath = screenshotPath,
                activityDescription = "Snapshot captured"
            )
            val updatedSession = session.copy(
                screenshots = session.screenshots + screenshotPath,
                segments = session.segments + newSegment
            )
            repository.saveSession(updatedSession)
            Logger.d("TrackingService") { "Updated session $sessionId with new screenshot" }
        }
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
