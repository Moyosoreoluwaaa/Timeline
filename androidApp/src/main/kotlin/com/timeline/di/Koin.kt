package com.timeline.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.timeline.analytics.Analytics
import com.timeline.analytics.FirebaseAnalyticsImpl
import com.timeline.data.TimelineDatabase
import com.timeline.data.TimelineRepository
import com.timeline.data.TimelineRepositoryImpl
import com.timeline.data.UserStorageManager
import com.timeline.data.createDataStore
import com.timeline.data.getDatabase
import com.timeline.data.repository.AuthRepositoryImpl
import com.timeline.domain.AndroidAppInfoProvider
import com.timeline.domain.AndroidDeviceUsageSyncer
import com.timeline.domain.AndroidNetworkMonitor
import com.timeline.domain.AndroidPermissionManager
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.NetworkMonitor
import com.timeline.domain.NotificationManager
import com.timeline.domain.PermissionManager
import com.timeline.domain.RevenueCatSubscriptionManager
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.TimelineExclusionPolicy
import com.timeline.domain.UserPreferences
import com.timeline.domain.auth.AuthUiHelper
import com.timeline.domain.ml.VisionAnalysisService
import com.timeline.domain.reasoning.GeminiReasoningService
import com.timeline.domain.reasoning.LocalHeuristicService
import com.timeline.domain.reasoning.PiiRedactor
import com.timeline.domain.reasoning.ReasoningService
import com.timeline.domain.repository.AuthRepository
import com.timeline.domain.usecase.MigrateGuestDataUseCase
import com.timeline.domain.usecase.SignInWithGoogleUseCase
import com.timeline.domain.usecase.SyncUserAccountUseCase
import com.timeline.ml.AndroidVisionAnalysisService
import com.timeline.notification.OneSignalManager
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.MetricsViewModel
import com.timeline.presentation.PaywallViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.presentation.RawHighlightViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.SimplifiedHighlightViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.TutorialViewModel
import com.timeline.util.auth.AndroidAuthUiHelper
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val platformModule: Module = module {
    single<TimelineDatabase> {
        getDatabase(get<Context>())
    }
    single { get<TimelineDatabase>().sessionDao() }
    single { get<TimelineDatabase>().reasoningDao() }
    single { get<TimelineDatabase>().analysisResultDao() }
    singleOf(::AndroidPermissionManager) { bind<PermissionManager>() }
    single { createDataStore(get<Context>()) }
    singleOf(::AndroidAppInfoProvider) { bind<AppInfoProvider>() }

    // Auth
    single { FirebaseAuth.getInstance() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::AndroidAuthUiHelper) { bind<AuthUiHelper>() }

    // Storage
    singleOf(::UserStorageManager)

    // Notifications
    singleOf(::OneSignalManager) { bind<NotificationManager>() }

    // Vision Analysis (ML Kit OCR & Image Labeling)
    singleOf(::AndroidVisionAnalysisService) { bind<VisionAnalysisService>() }

    // Device Real Usage Syncer
    singleOf(::AndroidDeviceUsageSyncer) { bind<DeviceUsageSyncer>() }

    // Network Monitor
    singleOf(::AndroidNetworkMonitor) { bind<NetworkMonitor>() }

    // Analytics
    single<Analytics> { FirebaseAnalyticsImpl(get<Context>()) }
}

val appModule = module {
    single { Logger(config = StaticConfig()) }
    singleOf(::TimelineRepositoryImpl) { bind<TimelineRepository>() }
    singleOf(::TimelineExclusionPolicy) { bind<ExclusionPolicy>() }
    singleOf(::UserPreferences)
    singleOf(::RevenueCatSubscriptionManager) { bind<SubscriptionManager>() }

    // Reasoning Layer
    single { PiiRedactor }
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
    viewModelOf(::RawHighlightViewModel)
    viewModelOf(::SimplifiedHighlightViewModel)
    viewModelOf(::TutorialViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
