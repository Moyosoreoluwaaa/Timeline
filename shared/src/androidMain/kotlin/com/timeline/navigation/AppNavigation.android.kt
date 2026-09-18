package com.timeline.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter
import com.timeline.domain.UserPreferences
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.PaywallViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.AppRootContainer
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialScreen
import com.timeline.tutorial.TutorialViewModel
import com.timeline.ui.AuthScreen
import com.timeline.ui.InsightsHostScreen
import com.timeline.ui.LocalNavAnimatedVisibilityScope
import com.timeline.ui.LocalSharedTransitionScope
import com.timeline.ui.PermissionScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.paywall.NewPaywallScreen
import com.timeline.ui.paywall.NewPaywallStyle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
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
    val tutorialViewModel: TutorialViewModel = koinViewModel()
    val newHighlightViewModel: com.timeline.presentation.NewHighlightViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val permissionViewModel: PermissionViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val userPreferences: UserPreferences = koinInject()
    val context = LocalContext.current

    val prefsState by userPreferences.state.collectAsStateWithLifecycle(null)
    val permState by permissionViewModel.state.collectAsStateWithLifecycle()
    val newHighlightState by newHighlightViewModel.state.collectAsStateWithLifecycle()

    val backStack = remember { mutableStateListOf<NavKey>() }

    // Check if launched from notification to open Highlight screen
    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        if (activity?.intent?.getBooleanExtra("navigate_to_highlight", false) == true) {
            activity.intent.removeExtra("navigate_to_highlight")
            backStack.add(Route.Highlight)
        }
    }

    // Direct navigation logic (bypassing Auth)
    LaunchedEffect(prefsState, permState.allGranted, backStack.lastOrNull()) {
        val state = prefsState ?: return@LaunchedEffect
        val currentRoute = backStack.lastOrNull()

        if (currentRoute == null) {
            val needsOnboarding = !state.isPermissionsCompleted || !permState.allGranted
            backStack.clear()
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
        val currentRoute = backStack.last()

        BackHandler(enabled = backStack.size > 1) {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            } else {
                onExitApp()
            }
        }

        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                AnimatedContent(
                    targetState = currentRoute,
                    label = "NavTransition",
                    transitionSpec = {
                        if (targetState is Route.FullScreenImage || initialState is Route.FullScreenImage) {
                            fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                        } else {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        }
                    }
                ) { route ->
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        when (route) {
                            is Route.Auth -> {
                                AuthScreen(
                                    viewModel = authViewModel,
                                    platformContext = context,
                                    onAuthSuccess = {
                                        backStack.clear()
                                        backStack.add(Route.Timeline)
                                    }
                                )
                            }

                            is Route.Permission -> {
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
                                    },
                                    onNavigateToPaywall = {
                                        backStack.add(Route.Paywall())
                                    }
                                )
                            }

                            is Route.Timeline -> {
                                LaunchedEffect(
                                    permState.allGranted,
                                    prefsState?.isPermissionsCompleted
                                ) {
                                    val isFullyGranted =
                                        permState.allGranted && prefsState?.isPermissionsCompleted == true
                                    if (isFullyGranted) {
                                        tutorialViewModel.onEvent(TutorialEvent.StartTutorial)
                                    }
                                }

                                AppRootContainer(
                                    timelineViewModel = timelineViewModel,
                                    tutorialViewModel = tutorialViewModel,
                                    onNavigateRoute = { routeName ->
                                        when (routeName) {
                                            TutorialScreen.SETTINGS.name -> backStack.add(Route.Settings)
                                            TutorialScreen.HIGHLIGHT.name -> backStack.add(Route.Highlight)
                                        }
                                    },
                                    onNavigateToSettings = {
                                        backStack.add(Route.Settings)
                                    },
                                    onNavigateToHighlight = {
                                        backStack.add(Route.Highlight)
                                    }
                                )
                            }

                            is Route.HighlightLoading -> {
                                com.timeline.ui.NewHighlightLoadingScreen(
                                    screenshots = newHighlightState.dynamicScreenshots,
                                    onFinished = {
                                        backStack.removeAt(backStack.size - 1)
                                        backStack.add(Route.Highlight)
                                    }
                                )
                            }

                            is Route.Highlight -> {
                                com.timeline.ui.NewHighlightScreen(
                                    viewModel = newHighlightViewModel,
                                    onNavigateBack = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    }
                                )
                            }

                            is Route.Insights -> {
                                InsightsHostScreen(
                                    onNavigateBack = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    }
                                )
                            }

                            is Route.Settings -> {
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onNavigateToPaywall = { isDeals ->
                                        backStack.add(Route.Paywall(isDealsVariant = isDeals))
                                    },
                                    onNavigateToCustomerCenter = {
                                        backStack.add(Route.CustomerCenter)
                                    },
                                    onNavigateToAuth = {
                                        backStack.add(Route.Auth)
                                    },
                                    onBack = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    }
                                )
                            }

                            is Route.CustomerCenter -> {
                                CustomerCenter(
                                    onDismiss = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    }
                                )
                            }

                            is Route.Paywall -> {
                                val paywallViewModel: PaywallViewModel = koinViewModel()
                                NewPaywallScreen(
                                    viewModel = paywallViewModel,
                                    style = if (route.isDealsVariant) NewPaywallStyle.LimitedOffer else NewPaywallStyle.Classic,
                                    onDismiss = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    },
                                    onPurchaseSuccess = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    }
                                )
                            }

                            is Route.FullScreenImage -> {}
                        }
                    }
                }
            }
        }
    }
}