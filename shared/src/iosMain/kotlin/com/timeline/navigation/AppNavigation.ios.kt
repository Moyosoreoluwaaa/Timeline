package com.timeline.navigation

import androidx.compose.runtime.*
import com.timeline.TimelineScreen
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.SetupScreen
import com.timeline.ui.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun AppNavigation(
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onStartService: () -> Unit
) {
    val setupViewModel: SetupViewModel = koinViewModel()
    val timelineViewModel: TimelineViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()

    var currentRoute by remember { mutableStateOf<Route>(Route.Setup) }

    when (currentRoute) {
        Route.Setup -> {
            SetupScreen(
                viewModel = setupViewModel,
                onNavigateToUsageStats = onNavigateToUsageStats,
                onNavigateToOverlay = onNavigateToOverlay,
                onNavigateToNotification = onNavigateToNotification,
                onNavigateToAccessibility = onNavigateToAccessibility,
                onComplete = { currentRoute = Route.Timeline }
            )
        }
        Route.Timeline -> {
            TimelineScreen(
                viewModel = timelineViewModel,
                onNavigateToSettings = { currentRoute = Route.Settings }
            )
        }
        Route.Settings -> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { currentRoute = Route.Timeline }
            )
        }
    }
}
