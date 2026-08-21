package com.timeline.di

import com.timeline.data.TimelineRepository
import com.timeline.data.TimelineRepositoryImpl
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.TimelineExclusionPolicy
import com.timeline.domain.UserPreferences
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.TimelineViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind

expect val platformModule: Module

val appModule = module {
    singleOf(::TimelineRepositoryImpl) { bind<TimelineRepository>() }
    single<ExclusionPolicy> { TimelineExclusionPolicy(get()) }
    singleOf(::UserPreferences)
    viewModelOf(::TimelineViewModel)
    viewModelOf(::SetupViewModel)
    viewModelOf(::SettingsViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
