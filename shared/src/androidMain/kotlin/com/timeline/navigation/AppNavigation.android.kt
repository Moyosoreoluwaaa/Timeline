package com.timeline.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.timeline.tutorial.TutorialViewModel
import com.timeline.ui.AuthScreen
import com.timeline.ui.InsightsHostScreen
import com.timeline.ui.LocalNavAnimatedVisibilityScope
import com.timeline.ui.LocalSharedTransitionScope
import com.timeline.ui.PermissionScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.paywall.NewPaywallScreen
import com.timeline.ui.paywall.NewPaywallStyle
import kotlinx.coroutines.launch
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
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()

    val backStack = remember { mutableStateListOf<NavKey>() }

    LaunchedEffect(tutorialViewModel) {
        tutorialViewModel.effects.collect { effect ->
            when (effect) {
                is com.timeline.tutorial.TutorialEffect.NavigateToScreen -> {
                    when (effect.screen) {
                        com.timeline.tutorial.TutorialScreen.TIMELINE -> {
                            while (backStack.size > 1 && backStack.last() != Route.Timeline) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                        com.timeline.tutorial.TutorialScreen.HIGHLIGHT -> {
                            if (backStack.lastOrNull() != Route.Highlight) {
                                backStack.add(Route.Highlight)
                            }
                        }
                        com.timeline.tutorial.TutorialScreen.SETTINGS -> {
                            if (backStack.lastOrNull() != Route.Settings) {
                                backStack.add(Route.Settings)
                            }
                        }
                    }
                }
                is com.timeline.tutorial.TutorialEffect.SetSheetExpanded -> {
                    timelineViewModel.onEvent(com.timeline.presentation.TimelineEvent.ToggleSheet(effect.expanded))
                }
                is com.timeline.tutorial.TutorialEffect.TriggerFullScreenImage -> {
                    if (effect.path != null) {
                        timelineViewModel.onEvent(com.timeline.presentation.TimelineEvent.ShowFullScreenImage(effect.path))
                    } else {
                        timelineViewModel.onEvent(com.timeline.presentation.TimelineEvent.DismissFullScreenImage)
                    }
                }
            }
        }
    }

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
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
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

                                    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1) { 3 }
                                    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

                                    LaunchedEffect(tutorialViewModel, pagerState) {
                                        tutorialViewModel.effects.collect { effect ->
                                            when (effect) {
                                                is com.timeline.tutorial.TutorialEffect.NavigateToScreen -> {
                                                    when (effect.screen) {
                                                        com.timeline.tutorial.TutorialScreen.TIMELINE -> {
                                                            pagerState.animateScrollToPage(1)
                                                        }
                                                        com.timeline.tutorial.TutorialScreen.HIGHLIGHT -> {
                                                            pagerState.animateScrollToPage(0)
                                                        }
                                                        com.timeline.tutorial.TutorialScreen.SETTINGS -> {
                                                            pagerState.animateScrollToPage(2)
                                                        }
                                                    }
                                                }
                                                is com.timeline.tutorial.TutorialEffect.SetSheetExpanded -> {
                                                    timelineViewModel.onEvent(com.timeline.presentation.TimelineEvent.ToggleSheet(effect.expanded))
                                                }
                                                is com.timeline.tutorial.TutorialEffect.TriggerFullScreenImage -> {
                                                    if (effect.path != null) {
                                                        timelineViewModel.onEvent(com.timeline.presentation.TimelineEvent.ShowFullScreenImage(effect.path))
                                                    } else {
                                                        timelineViewModel.onEvent(com.timeline.presentation.TimelineEvent.DismissFullScreenImage)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    androidx.compose.foundation.pager.HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        when (page) {
                                            0 -> {
                                                com.timeline.ui.NewHighlightScreen(
                                                    viewModel = newHighlightViewModel,
                                                    tutorialViewModel = tutorialViewModel,
                                                    onNavigateBack = {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(1)
                                                        }
                                                    }
                                                )
                                            }
                                            1 -> {
                                                AppRootContainer(
                                                    timelineViewModel = timelineViewModel,
                                                    tutorialViewModel = tutorialViewModel,
                                                    onNavigateToSettings = {
                                                        if (tutorialState.currentStep == com.timeline.tutorial.TutorialStep.SPOTLIGHT_SETTINGS_ICON) {
                                                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                                                        } else {
                                                            coroutineScope.launch {
                                                                pagerState.animateScrollToPage(2)
                                                            }
                                                        }
                                                    },
                                                    onNavigateToHighlight = {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(0)
                                                        }
                                                    }
                                                )
                                            }
                                            2 -> {
                                                SettingsScreen(
                                                    viewModel = settingsViewModel,
                                                    tutorialViewModel = tutorialViewModel,
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
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(1)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
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
                                        tutorialViewModel = tutorialViewModel,
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
                                        tutorialViewModel = tutorialViewModel,
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

                    com.timeline.tutorial.TutorialShowcaseOverlay(
                        state = tutorialState,
                        onEvent = tutorialViewModel::onEvent
                    )
                }
            }
        }
    }
}