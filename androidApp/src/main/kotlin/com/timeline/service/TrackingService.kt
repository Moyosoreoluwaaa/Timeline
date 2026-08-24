package com.timeline.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.milliseconds

class TrackingService : Service() {

    private val repository: TimelineRepository by inject()
    private val exclusionPolicy: ExclusionPolicy by inject()
    private val userPreferences: UserPreferences by inject()
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var trackingJob: Job? = null

    private lateinit var notificationHelper: TrackingNotificationHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var usageStatsHelper: UsageStatsHelper

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper = TrackingNotificationHelper(this)
        sessionManager = SessionManager(this, repository, serviceScope)
        usageStatsHelper = UsageStatsHelper(this, exclusionPolicy, userPreferences)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d { "TrackingService started" }
        startForeground(TrackingNotificationHelper.NOTIFICATION_ID, notificationHelper.buildNotification())
        startTracking()

        return START_STICKY
    }

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

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            var lastScreenshotTime = 0L
            while (isActive) {
                usageStatsHelper.pollUsageStats(
                    currentSessionId = sessionManager.currentSessionId,
                    onTransition = { currentApp ->
                        sessionManager.currentSessionId?.let { sessionManager.closePreviousSession(it) }
                        sessionManager.startNewSession(currentApp)
                        lastScreenshotTime = System.currentTimeMillis()
                    },
                    onPeriodicCheck = { currentApp ->
                        if (sessionManager.currentSessionId != null && (System.currentTimeMillis() - lastScreenshotTime) >= 60000L) {
                            Logger.withTag("TrackingService").d { "Periodic screenshot trigger for $currentApp" }
                            lastScreenshotTime = System.currentTimeMillis()
                            sessionManager.enqueueScreenshot(sessionManager.currentSessionId!!, currentApp)
                        }
                    },
                    onSessionEnd = {
                        sessionManager.currentSessionId?.let { sessionManager.closePreviousSession(it) }
                        sessionManager.clearCurrentSession()
                    },
                    onAccessibilityLost = {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(this@TrackingService, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            notificationHelper.showAccessibilityLostNotification()
                        }
                    }
                )
                delay(5000.milliseconds)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
