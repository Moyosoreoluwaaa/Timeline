package com.timeline

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.timeline.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TimelineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidLogger()
            androidContext(this@TimelineApp)
            // Phase 1: Logging integration with Koin
            logger(KermitKoinLogger(Logger.withTag("koin")))
        }

        Logger.d { "TimelineApp initialized with Koin" }
    }
}
