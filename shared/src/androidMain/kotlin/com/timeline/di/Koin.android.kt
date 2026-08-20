package com.timeline.di

import android.content.Context
import com.timeline.data.TimelineDatabase
import com.timeline.data.getDatabase
import com.timeline.data.getDatabaseBuilder
import com.timeline.domain.AndroidPermissionManager
import com.timeline.domain.PermissionManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TimelineDatabase> {
        getDatabase(getDatabaseBuilder(get<Context>()))
    }
    single { get<TimelineDatabase>().sessionDao() }
    singleOf(::AndroidPermissionManager) { bind<PermissionManager>() }
}
