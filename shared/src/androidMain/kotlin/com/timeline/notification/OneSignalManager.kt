package com.timeline.notification

import android.content.Context
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.timeline.domain.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OneSignalManager(private val context: Context) : NotificationManager {
    private var isInitialized = false

    override fun initialize(appId: String) {
        if (isInitialized) return
        
        // Set log level for debugging (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        
        // Initialize OneSignal
        OneSignal.initWithContext(context, appId)
        isInitialized = true
    }

    override suspend fun login(externalId: String) = withContext(Dispatchers.IO) {
        OneSignal.login(externalId)
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        OneSignal.logout()
    }

    override suspend fun setEmail(email: String) = withContext(Dispatchers.IO) {
        OneSignal.User.addEmail(email)
    }

    override suspend fun setSmsNumber(number: String) = withContext(Dispatchers.IO) {
        OneSignal.User.addSms(number)
    }

    override suspend fun setTag(key: String, value: String) = withContext(Dispatchers.IO) {
        OneSignal.User.addTag(key, value)
    }

    override fun hasPermission(): Boolean {
        return OneSignal.Notifications.permission
    }

    fun setLogLevel(level: LogLevel) {
        OneSignal.Debug.logLevel = level
    }
}
