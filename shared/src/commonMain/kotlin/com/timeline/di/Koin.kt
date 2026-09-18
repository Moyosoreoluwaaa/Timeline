package com.timeline.di

import com.timeline.data.TimelineRepository
import com.timeline.data.TimelineRepositoryImpl
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.TimelineExclusionPolicy
import com.timeline.domain.UserPreferences
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.RevenueCatSubscriptionManager
import com.timeline.domain.reasoning.ReasoningService
import com.timeline.domain.reasoning.GeminiReasoningService
import com.timeline.domain.reasoning.LocalHeuristicService
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.MetricsViewModel
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.presentation.PaywallViewModel
import com.timeline.presentation.HighlightViewModel
import com.timeline.presentation.NewHighlightViewModel
import com.timeline.domain.usecase.SignInWithGoogleUseCase
import com.timeline.domain.usecase.MigrateGuestDataUseCase
import com.timeline.domain.usecase.SyncUserAccountUseCase
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.timeline.tutorial.TutorialViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.bind

expect val platformModule: Module

val appModule = module {
    single { Logger(config = StaticConfig()) }
    singleOf(::TimelineRepositoryImpl) { bind<TimelineRepository>() }
    singleOf(::TimelineExclusionPolicy) { bind<ExclusionPolicy>() }
    singleOf(::UserPreferences)
    singleOf(::RevenueCatSubscriptionManager) { bind<SubscriptionManager>() }

    // Reasoning Layer
    single<ReasoningService> { GeminiReasoningService(get()) }
    single { LocalHeuristicService() }

    // Use Cases
    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::MigrateGuestDataUseCase)
    factoryOf(::SyncUserAccountUseCase)

    viewModelOf(::TimelineViewModel)
    viewModelOf(::MetricsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::PermissionViewModel)
    viewModelOf(::PaywallViewModel)
    viewModelOf(::HighlightViewModel)
    viewModelOf(::NewHighlightViewModel)
    viewModelOf(::TutorialViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
