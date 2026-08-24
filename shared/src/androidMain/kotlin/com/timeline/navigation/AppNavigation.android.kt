package com.timeline.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.timeline.TimelineScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.PermissionScreen
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.domain.UserPreferences
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

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
    val userPreferences: UserPreferences = koinInject()
    
    val prefsState by userPreferences.state.collectAsStateWithLifecycle(null)
    val permState by permissionViewModel.state.collectAsStateWithLifecycle()

    val backStack = remember { mutableStateListOf<NavKey>() }

    // Smart navigation logic
    LaunchedEffect(prefsState == null) {
        if (backStack.isEmpty() && prefsState != null) {
            val needsOnboarding = prefsState?.isPermissionsCompleted != true || !permState.allGranted
            if (needsOnboarding) {
                backStack.add(Route.Permission)
            } else {
                backStack.add(Route.Timeline)
            }
        }
    }

    // Auto-start service if all permissions are granted
    LaunchedEffect(permState.allGranted) {
        if (permState.allGranted) {
            onStartService()
        }
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            onBack = { 
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            },
            entryProvider = entryProvider {
                entry<Route.Permission> {
                    PermissionScreen(
                        viewModel = permissionViewModel,
                        onNavigateToUsageStats = onNavigateToUsageStats,
                        onNavigateToOverlay = onNavigateToOverlay,
                        onNavigateToNotification = onNavigateToNotification,
                        onNavigateToAccessibility = onNavigateToAccessibility,
                        onNavigateToBatteryOptimization = onNavigateToBatteryOptimization,
                        onAllGranted = {
                            backStack.clear()
                            backStack.add(Route.Timeline)
                        }
                    )
                }
                entry<Route.Timeline> {
                    TimelineScreen(
                        viewModel = timelineViewModel,
                        onNavigateToSettings = {
                            backStack.add(Route.Settings)
                        }
                    )
                }
                entry<Route.Settings> {
                    SettingsScreen(
                        viewModel = settingsViewModel
                    ) {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        }
                    }
                }
            }
        )
    }
}
