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
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.domain.UserPreferences
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

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
    val userPreferences: UserPreferences = koinInject()
    val setupState by setupViewModel.state.collectAsStateWithLifecycle()
    val prefsState by userPreferences.state.collectAsStateWithLifecycle(null)

    val backStack = remember { mutableStateListOf<NavKey>() }

    // Initialize backstack once
    if (backStack.isEmpty() && prefsState != null) {
        val initialRoute = if (prefsState!!.isSetupCompleted && setupState.isUsageStatsGranted && 
            setupState.isOverlayGranted && setupState.isNotificationGranted && 
            setupState.isAccessibilityGranted) {
            Route.Timeline
        } else {
            Route.Setup
        }
        backStack.add(initialRoute)
    }

    // React to setup completion if we are on Setup screen
    LaunchedEffect(prefsState?.isSetupCompleted) {
        if (prefsState?.isSetupCompleted == true && backStack.contains(Route.Setup)) {
            backStack.clear()
            backStack.add(Route.Timeline)
        }
    }

    LaunchedEffect(setupState.isUsageStatsGranted, setupState.isOverlayGranted, 
        setupState.isNotificationGranted, setupState.isAccessibilityGranted) {
        if (setupState.isUsageStatsGranted && setupState.isOverlayGranted && 
            setupState.isNotificationGranted && setupState.isAccessibilityGranted) {
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
                entry<Route.Setup> {
                    SetupScreen(
                        viewModel = setupViewModel,
                        onNavigateToUsageStats = onNavigateToUsageStats,
                        onNavigateToOverlay = onNavigateToOverlay,
                        onNavigateToNotification = onNavigateToNotification,
                        onNavigateToAccessibility = onNavigateToAccessibility
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
