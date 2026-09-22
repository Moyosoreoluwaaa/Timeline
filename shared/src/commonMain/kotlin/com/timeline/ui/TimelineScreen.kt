package com.timeline.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.BottomSummary
import com.timeline.ui.components.TimelineEntry
import com.timeline.ui.components.TimelineHeader
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel(),
    showTimeFilters: Boolean = false,
    onToggleTimeFilters: () -> Unit = {},
    showDatePicker: Boolean = false,
    onShowDatePickerChange: (Boolean) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHighlight: () -> Unit = {},
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {

        BackHandler(enabled = state.selectedSession != null || state.fullScreenImagePath != null) {
            if (state.fullScreenImagePath != null) {
                viewModel.onEvent(TimelineEvent.DismissFullScreenImage)
            } else {
                viewModel.onEvent(TimelineEvent.SelectSession(null))
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.selectedDate?.toEpochMilliseconds()
            )
            DatePickerDialog(
                onDismissRequest = { onShowDatePickerChange(false) },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEvent(
                                TimelineEvent.SelectDate(
                                    Instant.fromEpochMilliseconds(it)
                                )
                            )
                        }
                        onShowDatePickerChange(false)
                    }) { Text(AppStrings.TimelineOk) }
                }
            ) { DatePicker(state = datePickerState) }
        }

        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            topBar = {
                TimelineHeader(
                    selectedDate = state.selectedDate,
                    selectedFilter = state.timeFilter,
                    showTimeFilters = showTimeFilters,
                    onToggleTimeFilters = onToggleTimeFilters,
                    onFilterSelected = { viewModel.onEvent(TimelineEvent.FilterTime(it)) },
                    onNavigateToSettings = onNavigateToSettings,
                    onSelectDateClick = { onShowDatePickerChange(true) },
                    scrollBehavior = scrollBehavior,
                    onBoundsCalculated = onBoundsCalculated
                )
            }
        ) { padding ->
            val navBarPadding =
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val topPadding =
                (padding.calculateTopPadding() - TopAppBarCutoutRadius).coerceAtLeast(0.dp)

            Box(
                modifier = Modifier
                    .padding(top = topPadding)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.sessions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(AppWeights.Full)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    Text(
                                        AppStrings.TimelineNoActivity,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(AppWeights.Full)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    top = Dimensions.PaddingSmall,
                                    bottom = navBarPadding + (Dimensions.PaddingLarge * 2)
                                )
                            ) {
                                itemsIndexed(
                                    items = state.sessions,
                                    key = { _, session -> session.id }
                                ) { index, session ->
                                    val isMiddleItem = index == state.sessions.size / 2

                                    TimelineEntry(
                                        session = session,
                                        isFirst = index == 0,
                                        isLast = index == state.sessions.lastIndex,
                                        modifier = Modifier
                                            .animateItem()
                                            .then(
                                                if (isMiddleItem) {
                                                    Modifier.spotlightTarget(
                                                        TutorialStep.SPOTLIGHT_APP_ENTRY,
                                                        onBoundsCalculated
                                                    )
                                                } else Modifier
                                            )
                                    ) { viewModel.onEvent(TimelineEvent.SelectSession(session)) }
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                                if (coordinates.isAttached) {
                                    onBoundsCalculated(
                                        TutorialStep.SPOTLIGHT_SUMMARY_BAR,
                                        coordinates.boundsInWindow()
                                    )
                                }
                            }
                    ) {
                        BottomSummary(
                            summary = state.summary,
                            onSummaryClick = onNavigateToHighlight,
                            modifier = Modifier.spotlightTarget(
                                TutorialStep.SPOTLIGHT_SUMMARY_BAR,
                                onBoundsCalculated
                            )
                        )
                    }
                }
            }
        }

        // Bottom Sheet for selected session
        if (state.selectedSession != null) {
            val currentIndex = state.sessions.indexOfFirst { it.id == state.selectedSession?.id }
            val prevSession = if (currentIndex > 0) state.sessions[currentIndex - 1] else null
            val nextSession = if (currentIndex != -1 && currentIndex < state.sessions.lastIndex) state.sessions[currentIndex + 1] else null

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

            val expansionProgress by animateFloatAsState(
                targetValue = if (state.isSheetExpanded) 1f else 0f,
                label = "ExpansionProgress"
            )

            LaunchedEffect(sheetState.currentValue) {
                val isExpanded = sheetState.currentValue == androidx.compose.material3.SheetValue.Expanded
                if (isExpanded != state.isSheetExpanded) {
                    viewModel.onEvent(TimelineEvent.ToggleSheet(isExpanded))
                }
            }

            LaunchedEffect(state.isSheetExpanded) {
                if (state.isSheetExpanded && sheetState.currentValue != androidx.compose.material3.SheetValue.Expanded) {
                    sheetState.expand()
                } else if (!state.isSheetExpanded && sheetState.currentValue == androidx.compose.material3.SheetValue.Expanded) {
                    sheetState.partialExpand()
                }
            }

            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.onEvent(TimelineEvent.SelectSession(null))
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                SessionDetailSheet(
                    state = state,
                    expansionProgress = { expansionProgress },
                    prevSession = prevSession,
                    nextSession = nextSession,
                    onEvent = viewModel::onEvent,
                    onShowFullScreenImage = { path ->
                        viewModel.onEvent(TimelineEvent.ShowFullScreenImage(path))
                    },
                    onBoundsCalculated = onBoundsCalculated
                )
            }
        }

        // Full-Screen Image Overlay using Dialog to render on top of ModalBottomSheet
        val imagePath = state.fullScreenImagePath
        if (imagePath != null) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { viewModel.onEvent(TimelineEvent.DismissFullScreenImage) },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.onEvent(TimelineEvent.DismissFullScreenImage)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ScreenshotImage(
                        path = imagePath,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .spotlightTarget(
                                TutorialStep.FULL_SCREEN_IMAGE_PREVIEW,
                                onBoundsCalculated
                            )
                    )
                }
            }
        }
    }
}