package com.timeline.worker

import android.Manifest
import android.R
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        // Fetch daily total from Repository
        val totalUsage = "5h 42m" // Placeholder logic

        showSummaryNotification(totalUsage)
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSummaryNotification(totalUsage: String) {
        val notification = NotificationCompat.Builder(applicationContext, "summary_channel")
            .setContentTitle("Daily Summary")
            .setContentText("Total phone usage: $totalUsage")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .build()

        // Notification channel creation should happen here or in App startup
        NotificationManagerCompat.from(applicationContext).notify(2001, notification)
    }
}
