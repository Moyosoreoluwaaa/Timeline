package com.timeline.navigation

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
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
                onAllGranted = { currentRoute = Route.Timeline },
                onNavigateToPaywall = { currentRoute = Route.Paywall() }
            )
        }
        Route.Timeline -> {
            val pagerState = rememberPagerState(initialPage = 1) { 3 }
            val coroutineScope = rememberCoroutineScope()

            HorizontalPager(
                state = pagerState,
                modifier = fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        val highlightViewModel: com.timeline.presentation.NewHighlightViewModel = koinViewModel()
                        com.timeline.ui.NewHighlightScreen(
                            viewModel = highlightViewModel,
                            onNavigateBack = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                        )
                    }
                    1 -> {
                        TimelineScreen(
                            viewModel = timelineViewModel,
                            onNavigateToSettings = {
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            },
                            onNavigateToHighlight = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            },
                            onBoundsCalculated = { step, bounds -> }
                        )
                    }
                    2 -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateToPaywall = { isDeals -> currentRoute = Route.Paywall(isDeals) },
                            onNavigateToAuth = { currentRoute = Route.Auth },
                            onBack = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                        )
                    }
                }
            }
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
