package com.timeline

import android.app.Application
import androidx.work.Configuration
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.timeline.di.initKoin
import com.timeline.domain.NotificationManager
import com.timeline.domain.SecretConstants
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.configureRevenueCat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TimelineApp : Application(), Configuration.Provider {
    private val notificationManager: NotificationManager by inject()
    private val subscriptionManager: SubscriptionManager by inject()

    // App-lifetime scope for startup work that must not be tied to any screen
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@TimelineApp)
            // Phase 1: Logging integration with Koin
            logger(KermitKoinLogger(Logger.withTag("koin")))
        }

        // Initialize OneSignal
        notificationManager.initialize(SecretConstants.ONESIGNAL_APP_ID)

        // Initialize RevenueCat (must run before subscriptionManager.initialize())
        configureRevenueCat()

        // Load customer info + offerings at launch and register the purchases delegate.
        // Without this, isPro/customerInfo stay at their defaults until the paywall opens.
        appScope.launch {
            subscriptionManager.initialize()
        }

        // Schedule Background Highlight Digest Generation & Notifications
        com.timeline.worker.DigestScheduler.schedule(this)

        Logger.d { "TimelineApp initialized with Koin in process: ${getAppProcessName()}" }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    private fun getAppProcessName(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            // Fallback for older versions
            ""
        }
    }
}