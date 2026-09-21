package com.timeline.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onNavigateToSettings: () -> Unit,
    onNavigateToHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val timelineState by timelineViewModel.state.collectAsStateWithLifecycle()

    var showTimeFilters by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Sync tutorial data to timeline
    LaunchedEffect(tutorialState.tutorialSessions) {
        timelineViewModel.updateTutorialSessions(tutorialState.tutorialSessions)
    }

    // Auto-sync timeline & header overlay states with active tutorial step
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

            TutorialStep.SPOTLIGHT_TIME_FILTER_ICON -> {
                if (timelineState.selectedSession != null) {
                    timelineViewModel.onEvent(TimelineEvent.SelectSession(null))
                }
                showTimeFilters = false
                showDatePicker = false
            }

            TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION -> {
                showTimeFilters = true
                showDatePicker = false
            }

            TutorialStep.SPOTLIGHT_DATE_PICKER -> {
                showTimeFilters = false
                showDatePicker = true
            }

            TutorialStep.SPOTLIGHT_SUMMARY_BAR -> {
                showTimeFilters = false
                showDatePicker = false
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
            showTimeFilters = showTimeFilters,
            onToggleTimeFilters = { showTimeFilters = !showTimeFilters },
            showDatePicker = showDatePicker,
            onShowDatePickerChange = { showDatePicker = it },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToHighlight = onNavigateToHighlight,
            onBoundsCalculated = { step: TutorialStep, bounds: Rect ->
                tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
            }
        )
    }
}
