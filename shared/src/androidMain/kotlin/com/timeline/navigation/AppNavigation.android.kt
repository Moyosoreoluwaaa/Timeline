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
import com.timeline.ui.AuthScreen
import com.timeline.ui.LocalNavAnimatedVisibilityScope
import com.timeline.ui.LocalSharedTransitionScope
import com.timeline.ui.InsightsHostScreen
import com.timeline.ui.TimelineScreen
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
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val permissionViewModel: PermissionViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val userPreferences: UserPreferences = koinInject()
    val context = LocalContext.current
    
    val prefsState by userPreferences.state.collectAsStateWithLifecycle(null)
    val permState by permissionViewModel.state.collectAsStateWithLifecycle()

    val backStack = remember { mutableStateListOf<NavKey>() }

    // Smart navigation logic
    LaunchedEffect(prefsState, backStack.lastOrNull()) {
        val state = prefsState ?: return@LaunchedEffect
        val isLoggedIn = state.isLoggedIn
        val currentRoute = backStack.lastOrNull()
        
        if (!isLoggedIn) {
            if (currentRoute != Route.Auth) {
                backStack.clear()
                backStack.add(Route.Auth)
            }
        } else {
            if (currentRoute == null || currentRoute == Route.Auth) {
                val needsOnboarding = !state.isPermissionsCompleted || !permState.allGranted
                backStack.clear()
                if (needsOnboarding) {
                    backStack.add(Route.Permission)
                } else {
                    backStack.add(Route.Timeline)
                }
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

        BackHandler(enabled = backStack.size > 1 || currentRoute == Route.Auth) {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            } else if (currentRoute == Route.Auth) {
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
                                    onAuthSuccess = { }
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
                                    }
                                )
                            }
                            is Route.Timeline -> {
                                TimelineScreen(
                                    viewModel = timelineViewModel,
                                    onNavigateToSettings = {
                                        backStack.add(Route.Settings)
                                    },
                                    onNavigateToInsights = {
                                        backStack.add(Route.Insights)
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
                            is Route.FullScreenImage -> {
//                                FullScreenImageScreen(
//                                    path = route.path,
//                                    onBack = {
//                                        if (backStack.size > 1) {
//                                            backStack.removeAt(backStack.size - 1)
//                                        }
//                                    }
//                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
