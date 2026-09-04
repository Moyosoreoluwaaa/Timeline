package com.timeline.di

import com.timeline.data.TimelineDatabase
import com.timeline.data.getDatabase
import com.timeline.data.getDatabaseBuilder
import com.timeline.domain.NotificationManager
import com.timeline.domain.NoOpNotificationManager
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.NoOpAppInfoProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TimelineDatabase> {
        getDatabase(getDatabaseBuilder())
    }
    single { get<TimelineDatabase>().sessionDao() }

    single<NotificationManager> { NoOpNotificationManager() }
    single<AppInfoProvider> { NoOpAppInfoProvider() }
}
