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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter
import com.timeline.domain.Session
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.UserPreferences
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.PaywallViewModel
import com.timeline.presentation.PermissionViewModel
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.AppRootContainer
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialScreen.*
import com.timeline.tutorial.TutorialViewModel
import com.timeline.ui.AuthScreen
import com.timeline.ui.InsightsHostScreen
import com.timeline.ui.LocalNavAnimatedVisibilityScope
import com.timeline.ui.LocalSharedTransitionScope
import com.timeline.ui.PermissionScreen
import com.timeline.ui.SettingsScreen
import com.timeline.ui.components.SubscriptionBanner
import com.timeline.ui.paywall.NewPaywallScreen
import com.timeline.ui.paywall.NewPaywallStyle
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

// How long the "thank you for subscribing" banner stays visible
private const val THANK_YOU_BANNER_MS = 5000L

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
    val simplifiedHighlightViewModel: com.timeline.presentation.SimplifiedHighlightViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val permissionViewModel: PermissionViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val userPreferences: UserPreferences = koinInject()
    val subscriptionManager: SubscriptionManager = koinInject()
    val analytics: com.timeline.analytics.Analytics = koinInject()
    val repository: com.timeline.data.TimelineRepository = koinInject()
    val context = LocalContext.current

    val prefsState by userPreferences.state.collectAsStateWithLifecycle(null)
    val permState by permissionViewModel.state.collectAsStateWithLifecycle()
    val simplifiedHighlightState by simplifiedHighlightViewModel.state.collectAsStateWithLifecycle()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val isPro by subscriptionManager.isPro.collectAsStateWithLifecycle()
    val customerInfo by subscriptionManager.customerInfo.collectAsStateWithLifecycle()

    var sessionOpenedLogged by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!sessionOpenedLogged) {
            sessionOpenedLogged = true
            val sessions = repository.getTimeline().firstOrNull() ?: emptyList<Session>()
            val count = sessions.size
            val bucket = when {
                count == 0 -> "0"
                count in 1..10 -> "1-10"
                else -> "10+"
            }
            analytics.log(
                com.timeline.analytics.AnalyticsEvents.SESSION_OPENED,
                mapOf(com.timeline.analytics.AnalyticsParams.SESSIONS_BUCKET to bucket)
            )
        }
    }

    val backStack = remember { mutableStateListOf<NavKey>() }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000.milliseconds)
        showSplash = false
    }

    if (showSplash) {
        com.timeline.ui.SplashScreen(
            onSplashFinished = { showSplash = false }
        )
        return
    }

    // Subscription banner state lives here, above the nav host, so it survives route
    // changes: after a purchase the paywall pops and Timeline is composed from scratch,
    // which would otherwise lose the "just subscribed" moment.
    var showThankYou by remember { mutableStateOf(false) }
    LaunchedEffect(isPro, customerInfo != null) {
        if (isPro && customerInfo != null) {
            showThankYou = true
            kotlinx.coroutines.delay(THANK_YOU_BANNER_MS.milliseconds)
            showThankYou = false
        }
    }
    val subscriptionBanner = when {
        customerInfo == null -> SubscriptionBanner.None // RevenueCat hasn't answered yet
        isPro -> if (showThankYou) SubscriptionBanner.ThankYou else SubscriptionBanner.None
        else -> SubscriptionBanner.Upgrade
    }

    // NOTE: TutorialEffect.SetSheetExpanded / TriggerFullScreenImage no
    // longer exist. TimelineScreen now reads tutorialState.sheetLock
    // / requiredFullScreenImagePath directly (single source of truth owned
    // by TutorialViewModel), so this collector only needs to handle screen
    // navigation now -- pushing sheet/image state through here would just
    // reintroduce the two-writer race this change was meant to remove.
    LaunchedEffect(tutorialViewModel) {
        tutorialViewModel.effects.collect { effect ->
            when (effect) {
                is com.timeline.tutorial.TutorialEffect.NavigateToScreen -> {
                    when (effect.screen) {
                        TIMELINE -> {
                            while (backStack.size > 1 && backStack.last() != Route.Timeline) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                        HIGHLIGHT -> {
                            if (backStack.lastOrNull() != Route.Highlight) {
                                backStack.add(Route.Highlight)
                            }
                        }
                        SETTINGS -> {
                            if (backStack.lastOrNull() != Route.Settings) {
                                backStack.add(Route.Settings)
                            }
                        }

                        PAYWALL -> {
                            if (backStack.lastOrNull() !is Route.Paywall) {
                                backStack.add(Route.Paywall(source = "onboarding"))
                            }
                        }
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
            analytics.log(
                com.timeline.analytics.AnalyticsEvents.HIGHLIGHTS_OPENED,
                mapOf(com.timeline.analytics.AnalyticsParams.SOURCE to "notification")
            )
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val window = when {
                hour < 12 -> "morning"
                hour < 17 -> "afternoon"
                else -> "evening"
            }
            analytics.log(
                com.timeline.analytics.AnalyticsEvents.HIGHLIGHTS_NOTIFICATION_TAPPED,
                mapOf(com.timeline.analytics.AnalyticsParams.WINDOW to window)
            )
            backStack.add(Route.Highlight)
        }
    }

    // Direct navigation logic (bypassing Auth)
    LaunchedEffect(prefsState, permState.allGranted, backStack.lastOrNull()) {
        val state = prefsState ?: return@LaunchedEffect
        
        // If we are already on the Permission screen, we might stay there until granted
        val currentRoute = backStack.lastOrNull()
        val needsOnboarding = !state.isPermissionsCompleted || !permState.allGranted

        if (currentRoute == null) {
            backStack.clear()
            if (needsOnboarding) {
                backStack.add(Route.Permission)
            } else {
                backStack.add(Route.Timeline)
            }
        } else if (currentRoute == Route.Timeline && needsOnboarding) {
            // If user somehow revokes permissions while in the app, return them to permission screen
            backStack.add(Route.Permission)
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

        LaunchedEffect(currentRoute) {
            val screenName = when (currentRoute) {
                is Route.Permission -> "permission"
                is Route.Auth -> "auth"
                is Route.Paywall -> "paywall"
                is Route.CustomerCenter -> "customer_center"
                is Route.Insights -> "insights"
                is Route.Highlight, is Route.SimplifiedHighlight, is Route.RawHighlight -> "highlights"
                is Route.Settings -> "settings"
                is Route.HighlightLoading -> "highlight_loading"
                is Route.FullScreenImage -> "full_screen_image"
                is Route.Timeline -> null // Tracked via HorizontalPager pages
                else -> null
            }
            if (screenName != null) {
                analytics.log(
                    com.timeline.analytics.AnalyticsEvents.SCREEN_VIEW,
                    mapOf(com.timeline.analytics.AnalyticsParams.SCREEN_NAME to screenName)
                )
            }
        }

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
                                            tutorialViewModel.onEvent(TutorialEvent.StartTutorial)
                                            backStack.clear()
                                            backStack.add(Route.Timeline)
                                        }
                                    )
                                }

                                is Route.Timeline -> {
                                    LaunchedEffect(
                                        permState.allGranted,
                                        prefsState?.isPermissionsCompleted,
                                        prefsState?.isTutorial
                                    ) {
                                        val isFullyGranted =
                                            permState.allGranted && prefsState?.isPermissionsCompleted == true
                                        if (isFullyGranted && prefsState?.isTutorial == false && !tutorialState.isActive) {
                                            tutorialViewModel.onEvent(TutorialEvent.StartTutorial)
                                        }
                                    }

                                    AppRootContainer(
                                        timelineViewModel = timelineViewModel,
                                        tutorialViewModel = tutorialViewModel,
                                        onNavigateToSettings = {
                                            if (tutorialState.currentStep == com.timeline.tutorial.TutorialStep.SPOTLIGHT_SETTINGS_ICON) {
                                                tutorialViewModel.onEvent(TutorialEvent.NextStep)
                                            } else {
                                                backStack.add(Route.Settings)
                                            }
                                        },
                                        onNavigateToHighlight = {
                                            analytics.log(
                                                com.timeline.analytics.AnalyticsEvents.HIGHLIGHTS_OPENED,
                                                mapOf(com.timeline.analytics.AnalyticsParams.SOURCE to "summary_bar")
                                            )
                                            backStack.add(Route.Highlight)
                                        },
                                        subscriptionBanner = subscriptionBanner,
                                        onNavigateToPaywall = {
                                            if (backStack.lastOrNull() !is Route.Paywall) {
                                                backStack.add(Route.Paywall(source = "locked_feature"))
                                            }
                                        }
                                    )
                                }

                                 is Route.HighlightLoading -> {
                                    val loadingStyle = prefsState?.experiments?.highlightLoadingStyle ?: com.timeline.domain.model.HighlightLoadingStyle.CardStack
                                     when (loadingStyle) {
                                        com.timeline.domain.model.HighlightLoadingStyle.Arc -> com.timeline.ui.ArcHighlightLoadingScreen(
                                            screenshots = simplifiedHighlightState.dynamicScreenshots,
                                            onFinished = {
                                                backStack.removeAt(backStack.size - 1)
                                                backStack.add(Route.Highlight)
                                            }
                                        )
                                        com.timeline.domain.model.HighlightLoadingStyle.Stacked -> com.timeline.ui.StackedHighlightLoadingScreen(
                                            screenshots = simplifiedHighlightState.dynamicScreenshots,
                                            onFinished = {
                                                backStack.removeAt(backStack.size - 1)
                                                backStack.add(Route.Highlight)
                                            }
                                        )
                                        com.timeline.domain.model.HighlightLoadingStyle.CardStack -> com.timeline.ui.NewHighlightLoadingScreen(
                                            screenshots = simplifiedHighlightState.dynamicScreenshots,
                                            onFinished = {
                                                backStack.removeAt(backStack.size - 1)
                                                backStack.add(Route.Highlight)
                                            }
                                        )
                                    }
                                }

                                is Route.RawHighlight -> {
                                    val rawHighlightViewModel: com.timeline.presentation.RawHighlightViewModel = koinViewModel()
                                    com.timeline.ui.RawHighlightScreen(
                                        viewModel = rawHighlightViewModel,
                                        onNavigateBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }

                                is Route.SimplifiedHighlight -> {
                                    com.timeline.ui.SimplifiedHighlightScreen(
                                        viewModel = simplifiedHighlightViewModel,
                                        tutorialViewModel = tutorialViewModel,
                                        onNavigateBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }

                                is Route.Highlight -> {
                                    val variant = prefsState?.experiments?.highlightVariant ?: com.timeline.domain.model.HighlightVariant.Simplified
                                    if (variant == com.timeline.domain.model.HighlightVariant.RawDiagnostic) {
                                        val rawHighlightViewModel: com.timeline.presentation.RawHighlightViewModel = koinViewModel()
                                        com.timeline.ui.RawHighlightScreen(
                                            viewModel = rawHighlightViewModel,
                                            onNavigateBack = {
                                                if (backStack.size > 1) {
                                                    backStack.removeAt(backStack.size - 1)
                                                }
                                            }
                                        )
                                    } else {
                                        com.timeline.ui.SimplifiedHighlightScreen(
                                            viewModel = simplifiedHighlightViewModel,
                                            tutorialViewModel = tutorialViewModel,
                                            onNavigateBack = {
                                                if (backStack.size > 1) {
                                                    backStack.removeAt(backStack.size - 1)
                                                }
                                            }
                                        )
                                    }
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
                                            backStack.add(Route.Paywall(isDealsVariant = isDeals, source = "settings"))
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

                                    LaunchedEffect(route.source) {
                                        analytics.log(
                                            com.timeline.analytics.AnalyticsEvents.PAYWALL_VIEW,
                                            mapOf(com.timeline.analytics.AnalyticsParams.SOURCE to route.source)
                                        )
                                    }

                                    NewPaywallScreen(
                                        viewModel = paywallViewModel,
                                        style = if (route.isDealsVariant) NewPaywallStyle.LimitedOffer else NewPaywallStyle.Classic,
                                        onDismiss = {
                                            analytics.log(
                                                com.timeline.analytics.AnalyticsEvents.PAYWALL_DISMISSED,
                                                mapOf(com.timeline.analytics.AnalyticsParams.SOURCE to route.source)
                                            )
                                            tutorialViewModel.onEvent(TutorialEvent.SkipTutorial)
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        },
                                        onPurchaseSuccess = {
                                            tutorialViewModel.onEvent(TutorialEvent.SkipTutorial)
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