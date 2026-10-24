package com.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.INTRO_PAGE_COUNT
import com.timeline.presentation.OnboardingStep
import com.timeline.presentation.OnboardingStep.*
import com.timeline.presentation.PermissionEffect
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionViewModel
import com.timeline.ui.components.FanVariant
import com.timeline.ui.components.IntroPalette
import com.timeline.ui.components.LocalIntroPalette
import com.timeline.ui.components.OnboardingStepContent
import com.timeline.ui.components.OnboardingTopBar
import com.timeline.ui.components.backdropGlow
import com.timeline.ui.components.onboardingTween
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBatteryOptimization: () -> Unit,
    onAllGranted: () -> Unit,
    onExitApp: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PermissionEffect.NavigateToUsageStatsSettings -> onNavigateToUsageStats()
                is PermissionEffect.NavigateToOverlaySettings -> onNavigateToOverlay()
                is PermissionEffect.RequestNotificationPermission -> onNavigateToNotification()
                is PermissionEffect.NavigateToAccessibilitySettings -> onNavigateToAccessibility()
                is PermissionEffect.NavigateToBatteryOptimizationSettings -> onNavigateToBatteryOptimization()
                is PermissionEffect.AllGranted -> onAllGranted()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(PermissionEvent.CheckPermissions)
    }

    // Automatically navigate to tutorial after 1 second on Done screen
    LaunchedEffect(state.currentStep) {
        if (state.currentStep == Done) {
            delay(1000L.milliseconds)
            onAllGranted()
        }
    }

    // Determine if back should exit the app:
    // 1) Opening step (first step of onboarding)
    // 2) User was restored back into permission step and has no stepHistory
    val shouldExitOnBack = state.currentStep == Opening ||
            (state.isRestoredFlow && state.stepHistory.isEmpty() && state.currentStep == PermissionCardStack) ||
            (state.currentStep == Intro && state.introPage == 0 && state.stepHistory.isEmpty())

    BackHandler(enabled = state.currentStep != Done) {
        if (shouldExitOnBack) {
            onExitApp()
        } else {
            viewModel.onEvent(PermissionEvent.PreviousStep)
        }
    }

    val scheme = MaterialTheme.colorScheme
    val palette = remember(scheme) { IntroPalette.from(scheme) }

    CompositionLocalProvider(LocalIntroPalette provides palette) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .backdropGlow(FanVariant.CenteredFan, palette.accent, softer = palette.isLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                if (state.currentStep != Opening) {
                    val currentProgress = when (state.currentStep) {
                        Opening -> 0
                        Intro -> state.introPage
                        ThreePermissions -> INTRO_PAGE_COUNT
                        PermissionCardStack -> INTRO_PAGE_COUNT + 1 + state.activeCardIndex.coerceIn(
                            0,
                            state.permissions.lastIndex.coerceAtLeast(0)
                        )

                        Done -> INTRO_PAGE_COUNT + 1 + state.permissions.size
                    }
                    val totalProgress = INTRO_PAGE_COUNT + 1 + state.permissions.size

                    // Remove Skip from ThreePermissions and PermissionCardStack steps
                    val canSkip = state.currentStep == Intro

                    OnboardingTopBar(
                        current = currentProgress,
                        total = totalProgress,
                        canGoBack = state.currentStep != Done,
                        canSkip = canSkip,
                        onBack = {
                            if (shouldExitOnBack) {
                                onExitApp()
                            } else {
                                viewModel.onEvent(PermissionEvent.PreviousStep)
                            }
                        },
                        onSkip = {
                            if (state.currentStep == Intro) {
                                viewModel.onEvent(PermissionEvent.SkipIntro)
                            }
                        }
                    )
                }

                AnimatedContent(
                    targetState = state.currentStep,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    transitionSpec = {
                        val animationSpec = onboardingTween<IntOffset>(250)
                        val alphaSpec = onboardingTween<Float>(250)

                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(animationSpec) { it } + fadeIn(alphaSpec)).togetherWith(
                                slideOutHorizontally(animationSpec) { -it } + fadeOut(alphaSpec)
                            )
                        } else {
                            (slideInHorizontally(animationSpec) { -it } + fadeIn(alphaSpec)).togetherWith(
                                slideOutHorizontally(animationSpec) { it } + fadeOut(alphaSpec)
                            )
                        }
                    },
                    label = "OnboardingStep"
                ) { step ->
                    OnboardingStepContent(
                        step = step,
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}