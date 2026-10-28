package com.timeline.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.reasoning.HighlightContentSynthesizer
import com.timeline.domain.reasoning.ReasoningService
import com.timeline.presentation.TimeOfDayFilter
import com.timeline.service.TrackingNotificationHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DigestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: TimelineRepository by inject()
    private val reasoningService: ReasoningService by inject()
    private val appInfoProvider: AppInfoProvider by inject()
    private val userPreferences: com.timeline.domain.UserPreferences by inject()
    private val analytics: com.timeline.analytics.Analytics by inject()
    private val logger = Logger.withTag("DigestWorker")

    override suspend fun doWork(): Result {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val window = when {
            currentHour < 12 -> "morning"
            currentHour < 17 -> "afternoon"
            else -> "evening"
        }

        val prefs = userPreferences.state.firstOrNull()

        // Fire trial-ending notification if 1-3 days remain
        val daysLeft = com.timeline.presentation.MetricsAggregator.calculateDaysRemaining(prefs?.trialStartedAt)
        if (daysLeft in 1..3) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                TrackingNotificationHelper(applicationContext).showTrialEndingNotification(daysLeft)
            }
        }

        if (prefs?.isAgentReasoningEnabled == false) {
            analytics.log(
                com.timeline.analytics.AnalyticsEvents.HIGHLIGHTS_BATCH_FAILED,
                mapOf(com.timeline.analytics.AnalyticsParams.REASON to "disabled")
            )
            return Result.success()
        }

        val tz = TimeZone.currentSystemDefault()
        val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
        val dateString = today.toString()

        logger.i { "Starting background digest generation for $dateString (window: $window)" }

        val windowKey = "ALL_APPS_${window.uppercase()}"
        val existingWindow = repository.getAppDailyReasoning(windowKey, dateString).firstOrNull()
        if (existingWindow != null && window != "evening") {
            logger.i { "Digest already exists for $windowKey on $dateString, skipping." }
            return Result.success()
        }

        val allSessions = repository.getTimeline().firstOrNull() ?: emptyList()
        val todaySessions = allSessions.filter { it.startTime.toLocalDateTime(tz).date == today }

        if (todaySessions.isEmpty()) {
            logger.i { "No sessions recorded today on $dateString. Skipping digest generation." }
            return Result.success()
        }

        val windowFilter = when (window) {
            "morning" -> TimeOfDayFilter.MORNING
            "afternoon" -> TimeOfDayFilter.AFTERNOON
            else -> TimeOfDayFilter.EVENING
        }

        val windowSessions = todaySessions.filter { session ->
            val hour = session.startTime.toLocalDateTime(tz).hour
            when (windowFilter) {
                TimeOfDayFilter.MORNING -> hour in 6..11
                TimeOfDayFilter.AFTERNOON -> hour in 12..16
                TimeOfDayFilter.EVENING -> hour in 17..23 || hour in 0..5
                else -> true
            }
        }

        // Synthesize day-wide and window-specific content
        val dayRecap = HighlightContentSynthesizer.synthesizeDayRecap(todaySessions, appInfoProvider, tz)
        val windowRecap = HighlightContentSynthesizer.synthesizePeriodHighlight(windowFilter, windowSessions, appInfoProvider, tz)

        // Save window recap immediately
        repository.saveAppDailyReasoning(
            AppDailyReasoning(
                packageName = windowKey,
                date = dateString,
                summary = windowRecap.narrative,
                cognitiveMode = windowRecap.cognitiveMode,
                actionItems = windowRecap.actionItems
            )
        )

        // Extract sample OCR text from today's screenshots
        val screenshotPaths = todaySessions.flatMap { it.screenshots + it.segments.mapNotNull { s -> s.screenshotPath } }.distinct().take(8)
        val ocrDumps: List<String> = screenshotPaths.mapNotNull { path -> repository.getAnalysisResult(path)?.textResult?.fullText }.filter { it.isNotBlank() }

        val appSummaries = dayRecap.topApps.take(3).joinToString(", ") { "${it.displayName} (${it.totalMinutes}m)" }
        val summaryAppName = if (appSummaries.isNotBlank()) "Daily Summary ($appSummaries)" else "Daily Summary"

        val promptDumps = mutableListOf<String>()
        promptDumps.add(dayRecap.narrative)
        promptDumps.add("Top apps: $appSummaries")
        if (dayRecap.peakWindow != null) promptDumps.add("Peak: ${dayRecap.peakWindow}")
        if (ocrDumps.isNotEmpty()) promptDumps.addAll(ocrDumps.take(4))

        val startTime = System.currentTimeMillis()
        val result = reasoningService.generateDailyNarrative(
            packageName = "ALL_APPS",
            appName = summaryAppName,
            date = dateString,
            ocrDumps = promptDumps,
            labels = emptyList(),
            previousDaySummary = null
        )
        val latencyMs = System.currentTimeMillis() - startTime
        val latencyBucket = when {
            latencyMs < 5_000 -> "<5s"
            latencyMs < 15_000 -> "5-15s"
            latencyMs < 30_000 -> "15-30s"
            else -> "30s+"
        }

        result.onSuccess { reasoning ->
            repository.saveAppDailyReasoning(reasoning)

            val engine = if (com.timeline.domain.SecretConstants.GEMINI_API_KEY.isNotBlank() &&
                com.timeline.domain.SecretConstants.GEMINI_API_KEY != "your_gemini_api_key_here") "gemini" else "local_heuristic"

            analytics.log(
                com.timeline.analytics.AnalyticsEvents.HIGHLIGHTS_BATCH_DONE,
                mapOf(
                    com.timeline.analytics.AnalyticsParams.WINDOW to window,
                    com.timeline.analytics.AnalyticsParams.ENGINE to engine,
                    com.timeline.analytics.AnalyticsParams.LATENCY_BUCKET to latencyBucket
                )
            )

            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val notificationHelper = TrackingNotificationHelper(applicationContext)
                val notifTitle = if (window == "evening") "Your Day in Review" else "Your ${window.replaceFirstChar { it.uppercase() }} Highlights"
                val topAppName = dayRecap.topApps.firstOrNull()?.displayName
                val notifText = if (topAppName != null) {
                    "$topAppName (${dayRecap.topApps.first().totalMinutes}m) • ${dayRecap.totalMinutes}m total • Tap to explore"
                } else {
                    "Tap to see your daily narrative and insights."
                }
                notificationHelper.showDigestNotification(
                    title = notifTitle,
                    text = notifText,
                    window = window
                )
                logger.i { "Digest notification sent successfully." }
            }
        }.onFailure {
            logger.w(it) { "Background digest cloud generation failed, falling back to synthesized day recap" }
            repository.saveAppDailyReasoning(
                AppDailyReasoning(
                    packageName = "ALL_APPS",
                    date = dateString,
                    summary = dayRecap.narrative,
                    cognitiveMode = dayRecap.cognitiveMode,
                    actionItems = dayRecap.actionItems
                )
            )

            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val notificationHelper = TrackingNotificationHelper(applicationContext)
                val notifTitle = if (window == "evening") "Your Day in Review" else "Your ${window.replaceFirstChar { it.uppercase() }} Highlights"
                val topAppName = dayRecap.topApps.firstOrNull()?.displayName
                val notifText = if (topAppName != null) {
                    "$topAppName • ${dayRecap.totalMinutes}m total • Tap to explore"
                } else {
                    "Tap to see your daily narrative and insights."
                }
                notificationHelper.showDigestNotification(
                    title = notifTitle,
                    text = notifText,
                    window = window
                )
            }
        }

        return Result.success()
    }
}
