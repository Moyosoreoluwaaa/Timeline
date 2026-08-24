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

class TrackingNotificationHelper(private val context: Context) {

    init {
        createNotificationChannel()
    }

    fun buildNotification(): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Everett Tracking Active")
            .setContentText("Recording activity journal...")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAccessibilityLostNotification() {
        val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            context, 2, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Screenshot capture stopped")
            .setContentText("Tap to re-enable accessibility permission")
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ACCESSIBILITY_LOST, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timeline Tracking",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_ID_ACCESSIBILITY_LOST = 1002
        private const val CHANNEL_ID = "tracking_channel"
    }
}
