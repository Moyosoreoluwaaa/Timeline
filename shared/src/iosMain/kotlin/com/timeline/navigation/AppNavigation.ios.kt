package com.timeline.navigation

import androidx.compose.runtime.*
import com.timeline.ui.TimelineScreen
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.ui.PermissionScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.AuthScreen
import com.timeline.ui.InsightsHostScreen
import com.timeline.ui.paywall.NewPaywallScreen
import com.timeline.ui.paywall.NewPaywallStyle
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.PaywallViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun AppNavigation(
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBatteryOptimization: () -> Unit,
    onStartService: () -> Unit,
    onExitApp: () -> Unit
) {
    val timelineViewModel: TimelineViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val permissionViewModel: PermissionViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()

    var currentRoute by remember { mutableStateOf<Route>(Route.Permission) }

    when (currentRoute) {
        Route.Auth -> {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { currentRoute = Route.Timeline }
            )
        }
        Route.Permission -> {
            PermissionScreen(
                viewModel = permissionViewModel,
                onNavigateToUsageStats = onNavigateToUsageStats,
                onNavigateToOverlay = onNavigateToOverlay,
                onNavigateToNotification = onNavigateToNotification,
                onNavigateToAccessibility = onNavigateToAccessibility,
                onNavigateToBatteryOptimization = onNavigateToBatteryOptimization,
                onAllGranted = { currentRoute = Route.Timeline }
            )
        }
        Route.Timeline -> {
            TimelineScreen(
                viewModel = timelineViewModel,
                onNavigateToSettings = { currentRoute = Route.Settings },
                onNavigateToInsights = { currentRoute = Route.Insights }
            )
        }
        Route.Insights -> {
            InsightsHostScreen(
                onNavigateBack = { currentRoute = Route.Timeline }
            )
        }
        Route.Settings -> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToPaywall = { isDeals -> currentRoute = Route.Paywall(isDeals) },
                onNavigateToAuth = { /* TODO */ },
                onBack = { currentRoute = Route.Timeline }
            )
        }
        is Route.Paywall -> {
            val paywallViewModel: PaywallViewModel = koinViewModel()
            NewPaywallScreen(
                viewModel = paywallViewModel,
                style = if ((currentRoute as Route.Paywall).isDealsVariant) NewPaywallStyle.LimitedOffer else NewPaywallStyle.Classic,
                onDismiss = { currentRoute = Route.Timeline },
                onPurchaseSuccess = { currentRoute = Route.Timeline }
            )
        }
        else -> {}
    }
}
