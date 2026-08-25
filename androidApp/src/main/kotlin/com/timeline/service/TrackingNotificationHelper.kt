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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.TRACKING_CHANNEL_ID,
            "${AppStrings.AppName} Tracking",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = Constants.TRACKING_NOTIFICATION_ID
    }
}
