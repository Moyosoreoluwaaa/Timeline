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
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.Session
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Clock as KClock
import org.koin.android.ext.android.inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class TrackingService : Service() {

    private val repository: TimelineRepository by inject()
    private val exclusionPolicy: ExclusionPolicy by inject()
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

    private suspend fun pollUsageStats() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60000 // Look back 1 minute for reliability

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
            }
        }

        if (currentApp != null && currentApp != lastApp) {
            val isExcluded = exclusionPolicy.isExcluded(currentApp)
            if (isExcluded) {
                Logger.v { "App $currentApp is excluded by policy" }
                lastApp = currentApp
                return
            }

            Logger.i { "App switched: $lastApp -> $currentApp" }
            lastApp = currentApp
            lastStartTime = System.currentTimeMillis()
            
            serviceScope.launch {
                val session = Session(
                    id = java.util.UUID.randomUUID().toString(),
                    packageName = currentApp,
                    startTime = KClock.System.now(),
                    endTime = null,
                    durationMinutes = 0,
                    screenshots = emptyList(),
                    segments = emptyList()
                )
                repository.saveSession(session)
                Logger.d { "Saved new session for $currentApp" }
            }
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
