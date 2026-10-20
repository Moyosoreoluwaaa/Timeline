package com.timeline.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.PaywallViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.ui.AuthScreen
import com.timeline.ui.InsightsHostScreen
import com.timeline.ui.PermissionScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.TimelineScreen
import com.timeline.ui.paywall.NewPaywallScreen
import com.timeline.ui.paywall.NewPaywallStyle
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
                onNavigateToHighlight = { currentRoute = Route.Highlight },
                onBoundsCalculated = { step, bounds -> }
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
                onNavigateToAuth = { currentRoute = Route.Auth },
                onBack = { currentRoute = Route.Timeline }
            )
        }
        is Route.Paywall -> {
            val paywallViewModel: PaywallViewModel = koinViewModel()
            NewPaywallScreen(
                viewModel = paywallViewModel,
                style = if ((currentRoute as Route.Paywall).isDealsVariant) NewPaywallStyle.LimitedOffer else NewPaywallStyle.Classic,
                onDismiss = { currentRoute = Route.Timeline },
                onPurchaseSuccess = { _ -> currentRoute = Route.Timeline }
            )
        }
        else -> {}
    }
}
