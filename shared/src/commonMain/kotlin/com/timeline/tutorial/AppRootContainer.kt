package com.timeline.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.ui.TimelineScreen

@Composable
fun AppRootContainer(
    timelineViewModel: TimelineViewModel,
    tutorialViewModel: TutorialViewModel,
    onNavigateRoute: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val timelineState by timelineViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(tutorialViewModel) {
        tutorialViewModel.effects.collect { effect ->
            when (effect) {
                is TutorialEffect.NavigateToScreen -> onNavigateRoute(effect.screen.name)
                is TutorialEffect.SetSheetExpanded -> timelineViewModel.onEvent(TimelineEvent.ToggleSheet(effect.expanded))
                is TutorialEffect.TriggerFullScreenImage -> {
                    if (effect.path != null) {
                        timelineViewModel.onEvent(TimelineEvent.ShowFullScreenImage(effect.path))
                    } else {
                        timelineViewModel.onEvent(TimelineEvent.DismissFullScreenImage)
                    }
                }
            }
        }
    }

    // Auto-sync timeline session state based on active tutorial step
    LaunchedEffect(tutorialState.currentStep, timelineState.sessions) {
        when (tutorialState.currentStep) {
            TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL,
            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW,
            TutorialStep.EXPAND_BOTTOM_SHEET,
            TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR -> {
                if (timelineState.selectedSession == null && timelineState.sessions.isNotEmpty()) {
                    val middleIndex = timelineState.sessions.size / 2
                    timelineViewModel.onEvent(
                        TimelineEvent.SelectSession(timelineState.sessions[middleIndex])
                    )
                }
            }
            TutorialStep.SPOTLIGHT_SUMMARY_BAR -> {
                // Dismiss bottom sheet so summary bar at screen bottom is exposed in spotlight
                if (timelineState.selectedSession != null) {
                    timelineViewModel.onEvent(TimelineEvent.SelectSession(null))
                }
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        TimelineScreen(
            viewModel = timelineViewModel,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToHighlight = onNavigateToHighlight,
            onBoundsCalculated = { step: TutorialStep, bounds: Rect ->
                tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
            }
        )

        TutorialShowcaseOverlay(
            state = tutorialState,
            onEvent = tutorialViewModel::onEvent
        )
    }
}