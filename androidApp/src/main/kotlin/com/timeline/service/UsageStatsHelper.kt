package com.timeline.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import co.touchlab.kermit.Logger
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.flow.first

class UsageStatsHelper(
    private val context: Context,
    private val exclusionPolicy: ExclusionPolicy,
    private val userPreferences: UserPreferences
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    private var lastApp: String? = null
    private var wasAccessibilityGranted = true

    suspend fun pollUsageStats(
        currentSessionId: String?,
        onTransition: (String) -> Unit,
        onPeriodicCheck: (String) -> Unit,
        onSessionEnd: () -> Unit,
        onAccessibilityLost: () -> Unit
    ) {
        val prefs = userPreferences.state.first()
        if (!prefs.isUsageTrackingEnabled) {
            if (currentSessionId != null) onSessionEnd()
            lastApp = null
            return
        }

        checkAccessibilityStateChange(onAccessibilityLost)

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
                Logger.withTag("UsageStatsHelper").v { "Detected RESUMED event for: $currentApp" }
            }
        }

        if (currentApp != null) {
            if (currentApp != lastApp) {
                Logger.withTag("UsageStatsHelper").d { "App transition detected: $lastApp -> $currentApp" }

                val isExcluded = exclusionPolicy.isExcluded(currentApp)
                if (isExcluded) {
                    Logger.withTag("UsageStatsHelper").d { "App $currentApp is excluded, skipping" }
                    if (currentSessionId != null) onSessionEnd()
                    lastApp = currentApp
                    return
                }

                onTransition(currentApp)
                lastApp = currentApp
            } else {
                onPeriodicCheck(currentApp)
            }
        }
    }

    private fun checkAccessibilityStateChange(onAccessibilityLost: () -> Unit) {
        val isGranted = TimelineAccessibilityService.getInstance() != null
        if (wasAccessibilityGranted && !isGranted) {
            Logger.withTag("UsageStatsHelper").w { "Accessibility service disconnected" }
            onAccessibilityLost()
        }
        wasAccessibilityGranted = isGranted
    }
}
