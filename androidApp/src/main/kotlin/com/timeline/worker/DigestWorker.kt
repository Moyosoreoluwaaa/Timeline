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
    private val logger = Logger.withTag("DigestWorker")

    override suspend fun doWork(): Result {
        val tz = TimeZone.currentSystemDefault()
        val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
        val dateString = today.toString()

        logger.i { "Starting background digest generation for $dateString" }

        val existing = repository.getAppDailyReasoning("ALL_APPS", dateString).firstOrNull()
        if (existing != null) {
            logger.i { "Digest already exists for $dateString, skipping." }
            return Result.success()
        }

        val sessions = repository.getTimeline().firstOrNull() ?: emptyList()
        val appNames = sessions.map { it.packageName }.distinct().take(3).joinToString(", ")
        
        val result = reasoningService.generateDailyNarrative(
            packageName = "ALL_APPS",
            appName = "Daily Summary ($appNames)",
            date = dateString,
            ocrDumps = listOf("Active usage today across $appNames"),
            labels = emptyList(),
            previousDaySummary = null
        )

        result.onSuccess { reasoning ->
            repository.saveAppDailyReasoning(reasoning)
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val notificationHelper = TrackingNotificationHelper(applicationContext)
                notificationHelper.showDigestNotification(
                    title = "Your Highlights are ready",
                    text = "Tap to see your daily narrative and insights."
                )
                logger.i { "Digest notification sent successfully." }
            }
        }.onFailure {
            logger.e(it) { "Background digest generation failed" }
        }

        return Result.success()
    }
}
