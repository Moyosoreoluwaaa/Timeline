package com.timeline

import android.app.Application
import androidx.work.Configuration
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.timeline.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TimelineApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidLogger()
            androidContext(this@TimelineApp)
            // Phase 1: Logging integration with Koin
            logger(KermitKoinLogger(Logger.withTag("koin")))
        }

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
