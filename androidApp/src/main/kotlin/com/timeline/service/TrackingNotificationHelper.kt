package com.timeline.service

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timeline.util.AppStrings
import com.timeline.util.Constants

class TrackingNotificationHelper(private val context: Context) {

    init {
        createNotificationChannel()
    }

    fun buildNotification(): Notification =
        NotificationCompat.Builder(context, Constants.TRACKING_CHANNEL_ID)
            .setContentTitle(AppStrings.NotificationTrackingActiveTitle)
            .setContentText(AppStrings.NotificationTrackingActiveContent)
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAccessibilityLostNotification() {
        val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            context, 2, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, Constants.TRACKING_CHANNEL_ID)
            .setContentTitle(AppStrings.NotificationAccessibilityLostTitle)
            .setContentText(AppStrings.NotificationAccessibilityLostContent)
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(Constants.ACCESSIBILITY_LOST_NOTIFICATION_ID, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDigestNotification(title: String, text: String) {
        val intent = Intent(context, Class.forName("com.timeline.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_highlight", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, Constants.DIGEST_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(Constants.DIGEST_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        
        val trackingChannel = NotificationChannel(
            Constants.TRACKING_CHANNEL_ID,
            "${AppStrings.AppName} Tracking",
            NotificationManager.IMPORTANCE_MIN
        )
        manager.createNotificationChannel(trackingChannel)

        val digestChannel = NotificationChannel(
            Constants.DIGEST_CHANNEL_ID,
            "${AppStrings.AppName} Highlights",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily activity digests and narrative insights"
        }
        manager.createNotificationChannel(digestChannel)
    }

    companion object {
        const val NOTIFICATION_ID = Constants.TRACKING_NOTIFICATION_ID
    }
}
