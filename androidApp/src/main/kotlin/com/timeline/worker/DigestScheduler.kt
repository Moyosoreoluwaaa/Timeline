package com.timeline.worker

import android.content.Context
import androidx.work.*
import co.touchlab.kermit.Logger
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DigestScheduler {
    private val logger = Logger.withTag("DigestScheduler")

    private const val PERIODIC_WORK_TAG = "periodic_highlight_digest_work"
    private const val TARGET_WORK_TAG = "target_hour_highlight_digest_work"

    fun schedule(context: Context) {
        schedulePeriodicWork(context)
        scheduleTargetHourWork(context)
    }

    private fun schedulePeriodicWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DigestWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(PERIODIC_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        logger.i { "Enqueued periodic highlight digest work (every 3 hours)" }
    }

    private fun scheduleTargetHourWork(context: Context) {
        val targetHours = listOf(12, 17, 21) // 12:00 PM, 5:00 PM, 9:00 PM
        val now = Calendar.getInstance()

        targetHours.forEach { targetHour ->
            val targetCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val initialDelayMs = targetCalendar.timeInMillis - now.timeInMillis
            val oneTimeRequest = OneTimeWorkRequestBuilder<DigestWorker>()
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(TARGET_WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${TARGET_WORK_TAG}_$targetHour",
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            logger.i { "Scheduled target digest work for $targetHour:00 (delay: ${initialDelayMs / 60000} minutes)" }
        }
    }
}
