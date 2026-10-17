package com.timeline.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.reasoning.ReasoningService
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

        val existing = repository.getAppDailyReasoning("ALL_APPS", dateString).firstOrNull()
        if (existing != null) {
            logger.i { "Digest already exists for $dateString, skipping." }
            return Result.success()
        }

        val sessions = repository.getTimeline().firstOrNull() ?: emptyList()
        val appNames = sessions.map { it.packageName }.distinct().take(3).joinToString(", ")
        
        val startTime = System.currentTimeMillis()
        val result = reasoningService.generateDailyNarrative(
            packageName = "ALL_APPS",
            appName = "Daily Summary ($appNames)",
            date = dateString,
            ocrDumps = listOf("Active usage today across $appNames"),
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
                notificationHelper.showDigestNotification(
                    title = "Your Highlights are ready",
                    text = "Tap to see your daily narrative and insights.",
                    window = window
                )
                logger.i { "Digest notification sent successfully." }
            }
        }.onFailure {
            val reason = when {
                it.message?.contains("quota", ignoreCase = true) == true || it.message?.contains("429") == true -> "quota"
                it.message?.contains("parse", ignoreCase = true) == true || it.message?.contains("json", ignoreCase = true) == true -> "parse_error"
                it.message?.contains("disabled", ignoreCase = true) == true -> "disabled"
                else -> "network"
            }
            analytics.log(
                com.timeline.analytics.AnalyticsEvents.HIGHLIGHTS_BATCH_FAILED,
                mapOf(com.timeline.analytics.AnalyticsParams.REASON to reason)
            )
            logger.e(it) { "Background digest generation failed" }
        }

        return Result.success()
    }
}
