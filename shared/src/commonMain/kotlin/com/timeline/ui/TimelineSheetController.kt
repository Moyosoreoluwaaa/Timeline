package com.timeline.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.SheetLock
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialGesture
import com.timeline.tutorial.TutorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
internal class TimelineSheetController(
    val scaffoldState: BottomSheetScaffoldState,
    val expansionProgress: State<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberTimelineSheetController(
    hasSession: Boolean,
    activeSheetLock: SheetLock?,
    isStateSheetExpanded: Boolean,
    viewModel: TimelineViewModel,
    tutorialViewModel: TutorialViewModel
): TimelineSheetController {
    val lockState = rememberUpdatedState(activeSheetLock)
    val hasSessionState = rememberUpdatedState(hasSession)

    val sheetState = rememberStandardBottomSheetState(
        initialValue = if (hasSession) SheetValue.PartiallyExpanded else SheetValue.Hidden,
        skipHiddenState = false,
        confirmValueChange = { target ->
            if (!hasSessionState.value) {
                target == SheetValue.Hidden
            } else {
                when (val lock = lockState.value) {
                    is SheetLock.Fixed -> (target == SheetValue.Expanded) == lock.expanded
                    is SheetLock.Free -> true
                    null -> true
                }
            }
        }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    val isSheetActuallyExpanded = sheetState.currentValue == SheetValue.Expanded
    val expansionProgress = animateFloatAsState(
        targetValue = if (isSheetActuallyExpanded) 1f else 0f,
        label = "ExpansionProgress"
    )

    val targetSheetValue = when {
        !hasSession -> SheetValue.Hidden
        activeSheetLock is SheetLock.Fixed -> if (activeSheetLock.expanded) SheetValue.Expanded else SheetValue.PartiallyExpanded
        activeSheetLock is SheetLock.Free -> if (activeSheetLock.startExpanded) SheetValue.Expanded else SheetValue.PartiallyExpanded
        isStateSheetExpanded -> SheetValue.Expanded
        else -> SheetValue.PartiallyExpanded
    }

    LaunchedEffect(targetSheetValue) {
        if (sheetState.targetValue != targetSheetValue) {
            when (targetSheetValue) {
                SheetValue.Hidden -> sheetState.hide()
                SheetValue.PartiallyExpanded -> sheetState.partialExpand()
                SheetValue.Expanded -> sheetState.expand()
            }
        }
    }

    LaunchedEffect(sheetState.currentValue) {
        val isExpanded = sheetState.currentValue == SheetValue.Expanded
        when (val lock = activeSheetLock) {
            null -> {
                if (isExpanded != isStateSheetExpanded) {
                    viewModel.onEvent(TimelineEvent.ToggleSheet(isExpanded))
                }
                if (sheetState.currentValue == SheetValue.Hidden && hasSession) {
                    viewModel.onEvent(TimelineEvent.SelectSession(null))
                }
            }
            is SheetLock.Free -> {
                if (isExpanded != lock.startExpanded) {
                    tutorialViewModel.onEvent(
                        TutorialEvent.RealGestureObserved(
                            if (isExpanded) TutorialGesture.EXPANDED_SHEET else TutorialGesture.COLLAPSED_SHEET
                        )
                    )
                }
            }
            is SheetLock.Fixed -> {}
        }
    }

    return TimelineSheetController(scaffoldState, expansionProgress)
}
