package com.timeline.di

import android.content.Context
import com.timeline.data.TimelineDatabase
import com.timeline.data.getDatabase
import com.timeline.data.getDatabaseBuilder
import com.timeline.data.createDataStore
import com.timeline.domain.AndroidAppInfoProvider
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.AndroidPermissionManager
import com.timeline.domain.PermissionManager
import com.timeline.domain.repository.AuthRepository
import com.timeline.domain.auth.AuthUiHelper
import com.timeline.data.repository.AuthRepositoryImpl
import com.timeline.util.auth.AndroidAuthUiHelper
import com.google.firebase.auth.FirebaseAuth
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
    single { createDataStore(get<Context>()) }
    singleOf(::AndroidAppInfoProvider) { bind<AppInfoProvider>() }
    
    // Auth
    single { FirebaseAuth.getInstance() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::AndroidAuthUiHelper) { bind<AuthUiHelper>() }
}
