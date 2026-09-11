package com.timeline.di

import com.timeline.data.TimelineDatabase
import com.timeline.data.getDatabase
import com.timeline.data.getDatabaseBuilder
import com.timeline.data.UserStorageManager
import com.timeline.domain.NotificationManager
import com.timeline.domain.NoOpNotificationManager
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.NoOpAppInfoProvider
import com.timeline.domain.ml.VisionAnalysisService
import com.timeline.domain.ml.NoOpVisionAnalysisService
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.NoOpDeviceUsageSyncer
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TimelineDatabase> {
        getDatabase(getDatabaseBuilder())
    }
    single { get<TimelineDatabase>().sessionDao() }
    single { get<TimelineDatabase>().reasoningDao() }
    single { get<TimelineDatabase>().analysisResultDao() }
    singleOf(::UserStorageManager)

    single<NotificationManager> { NoOpNotificationManager() }
    single<AppInfoProvider> { NoOpAppInfoProvider() }
    
    // Vision Analysis (iOS No-Op)
    single<VisionAnalysisService> { NoOpVisionAnalysisService() }

    // Device Real Usage Syncer (iOS No-Op)
    single<DeviceUsageSyncer> { NoOpDeviceUsageSyncer() }
}
