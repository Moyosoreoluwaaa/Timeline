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

    // Steps with no per-step UI wiring below still need their filter/date
    // panel visibility set. This stays separate from sheet/full-screen-image
    // ownership, which TutorialViewModel now owns directly via
    // sheetLock / requiredFullScreenImagePath (read inside
    // TimelineScreen), so this effect no longer touches those two.
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
                showTimeFilters = false
                showDatePicker = false
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

            TutorialStep.SPOTLIGHT_DATE_CONTAINER -> {
                showTimeFilters = false
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

    // Real gestures -> tutorial progress. This is what lets a genuine tap on
    // the thumbnail, sheet, or session nav buttons advance the tutorial,
    // instead of the tutorial only knowing about taps on its own card.
    // Only forwards while the tutorial is active, so normal use of the app
    // never talks to TutorialViewModel.
    LaunchedEffect(tutorialState.isActive) {
        if (!tutorialState.isActive) return@LaunchedEffect
        timelineViewModel.realInteractions.collect { event ->
            val gesture = when (event) {
                is TimelineEvent.SelectSession ->
                    if (event.session != null) TutorialGesture.TAPPED_SESSION_ENTRY else null
                is TimelineEvent.ShowFullScreenImage ->
                    if (event.path != null) TutorialGesture.TAPPED_THUMBNAIL else null
                is TimelineEvent.DismissFullScreenImage ->
                    TutorialGesture.DISMISSED_FULL_SCREEN_IMAGE
                is TimelineEvent.ToggleSheet ->
                    if (event.expanded) TutorialGesture.EXPANDED_SHEET else TutorialGesture.COLLAPSED_SHEET
                is TimelineEvent.SelectPreviousSession -> TutorialGesture.TAPPED_PREV_SESSION
                is TimelineEvent.SelectNextSession -> TutorialGesture.TAPPED_NEXT_SESSION
                else -> null
            }
            if (gesture != null) {
                tutorialViewModel.onEvent(TutorialEvent.RealGestureObserved(gesture))
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        TimelineScreen(
            viewModel = timelineViewModel,
            tutorialViewModel = tutorialViewModel,
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