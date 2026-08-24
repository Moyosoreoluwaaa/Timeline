package com.timeline.navigation

import androidx.compose.runtime.*
import com.timeline.TimelineScreen
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.ui.PermissionScreen
import com.timeline.ui.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun AppNavigation(
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBatteryOptimization: () -> Unit,
    onStartService: () -> Unit
) {
    val timelineViewModel: TimelineViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val permissionViewModel: PermissionViewModel = koinViewModel()

    var currentRoute by remember { mutableStateOf<Route>(Route.Permission) }

    when (currentRoute) {
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
                onNavigateToSettings = { currentRoute = Route.Settings }
            )
        }
        Route.Settings -> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { currentRoute = Route.Timeline }
            )
        }
        else -> {}
    }
}
