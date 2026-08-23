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
import com.timeline.ui.SetupScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.AuthScreen
import com.timeline.ui.PermissionScreen
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.AuthViewModel
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
    val setupViewModel: SetupViewModel = koinViewModel()
    val timelineViewModel: TimelineViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val permissionViewModel: PermissionViewModel = koinViewModel()
    val userPreferences: UserPreferences = koinInject()
    val setupState by setupViewModel.state.collectAsStateWithLifecycle()
    val prefsState by userPreferences.state.collectAsStateWithLifecycle(null)

    val backStack = remember { mutableStateListOf<NavKey>() }

    val allPermissionsGranted = setupState.isUsageStatsGranted && 
            setupState.isOverlayGranted && 
            setupState.isNotificationGranted && 
            setupState.isAccessibilityGranted &&
            setupState.isBatteryOptimizationDisabled

    // Initialize backstack once when preferences are loaded
    LaunchedEffect(prefsState == null) {
        if (backStack.isEmpty() && prefsState != null) {
            // Start with Auth for testing
            backStack.add(Route.Auth)
        }
    }

    // React to setup completion or late permission grants if we are stuck on Setup screen
    LaunchedEffect(prefsState?.isSetupCompleted, allPermissionsGranted) {
        if (prefsState?.isSetupCompleted == true && allPermissionsGranted && backStack.contains(Route.Setup)) {
            backStack.clear()
            backStack.add(Route.Timeline)
        }
    }

    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
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
                entry<Route.Auth> {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            backStack.add(Route.PermissionTest)
                        }
                    )
                }
                entry<Route.PermissionTest> {
                    PermissionScreen(
                        viewModel = permissionViewModel,
                        onAllGranted = {
                            backStack.clear()
                            backStack.add(Route.Timeline)
                        }
                    )
                }
                entry<Route.Setup> {
                    SetupScreen(
                        viewModel = setupViewModel,
                        onNavigateToUsageStats = onNavigateToUsageStats,
                        onNavigateToOverlay = onNavigateToOverlay,
                        onNavigateToNotification = onNavigateToNotification,
                        onNavigateToAccessibility = onNavigateToAccessibility,
                        onNavigateToBatteryOptimization = onNavigateToBatteryOptimization
                    ) {
                        backStack.clear()
                        backStack.add(Route.Timeline)
                    }
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
