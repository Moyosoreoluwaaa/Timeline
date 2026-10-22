package com.timeline.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialGesture
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.ui.components.ImageHeroOverlay
import com.timeline.ui.components.SubscriptionBanner
import com.timeline.ui.components.TimelineHeader
import com.timeline.ui.components.TopAppBarCutoutRadius
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

private val SheetPeekHeight = 400.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel(),
    tutorialViewModel: TutorialViewModel = koinViewModel(),
    showTimeFilters: Boolean = false,
    onToggleTimeFilters: () -> Unit = {},
    showDatePicker: Boolean = false,
    onShowDatePickerChange: (Boolean) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHighlight: () -> Unit = {},
    subscriptionBanner: SubscriptionBanner = SubscriptionBanner.None,
    onNavigateToPaywall: () -> Unit = {},
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val isShowingShimmer = state.isLoading || (tutorialState.isActive && state.sessions.isEmpty())

    val activeSheetLock = if (tutorialState.isActive) tutorialState.sheetLock else null
    val tutorialControlsFullScreenImage =
        tutorialState.isActive && tutorialState.currentStep == TutorialStep.FULL_SCREEN_IMAGE_PREVIEW
    val effectiveFullScreenImagePath = if (tutorialControlsFullScreenImage) {
        tutorialState.requiredFullScreenImagePath
    } else {
        state.fullScreenImagePath
    }

    val lastSelectedSession = remember { mutableStateOf<Session?>(null) }
    LaunchedEffect(state.selectedSession) {
        if (state.selectedSession != null) lastSelectedSession.value = state.selectedSession
    }
    val sessionToDisplay = state.selectedSession ?: lastSelectedSession.value ?: state.sessions.firstOrNull() ?: Session(
        id = "placeholder", packageName = "", displayName = "", startTime = Clock.System.now(), endTime = Clock.System.now()
    )

    val currentIndex = state.sessions.indexOfFirst { it.id == state.selectedSession?.id }
    val prevSession = if (currentIndex > 0) state.sessions[currentIndex - 1] else null
    val nextSession = if (currentIndex != -1 && currentIndex < state.sessions.lastIndex) state.sessions[currentIndex + 1] else null

    val density = LocalDensity.current
    var containerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerHeight by remember { mutableStateOf(0.dp) }
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Include the nav bar inset so the peeked sheet content isn't hidden under system buttons
    var imageOrigin by remember { mutableStateOf<Rect?>(null) }
    val peekHeight: Dp = SheetPeekHeight + navBarPadding
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val hasSession = state.selectedSession != null
    val sheetController = rememberTimelineSheetController(
        hasSession = hasSession,
        activeSheetLock = activeSheetLock,
        isStateSheetExpanded = state.isSheetExpanded,
        viewModel = viewModel,
        tutorialViewModel = tutorialViewModel,
        containerHeightPx = containerHeightPx,
        peekHeightPx = with(density) { peekHeight.toPx() }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeightPx = it.height.toFloat() }
    ) {
        BackHandler(enabled = state.selectedSession != null || effectiveFullScreenImagePath != null) {
            if (effectiveFullScreenImagePath != null) {
                if (tutorialControlsFullScreenImage) {
                    tutorialViewModel.onEvent(
                        TutorialEvent.RealGestureObserved(TutorialGesture.DISMISSED_FULL_SCREEN_IMAGE)
                    )
                } else {
                    viewModel.onEvent(TimelineEvent.DismissFullScreenImage)
                }
            } else {
                viewModel.onEvent(TimelineEvent.SelectSession(null))
            }
        }

        if (showDatePicker) {
            TimelineDatePickerDialog(
                selectedDate = state.selectedDate,
                onDateSelected = { viewModel.onEvent(TimelineEvent.SelectDate(it)) },
                onDismiss = { onShowDatePickerChange(false) }
            )
        }

        // The sheet scaffold now owns the whole screen. The header lives in its body,
        // so the sheet (always placed after the body) draws over the header.
        BottomSheetScaffold(
            scaffoldState = sheetController.scaffoldState,
            modifier = Modifier.fillMaxSize(),
            sheetPeekHeight = peekHeight,
            sheetSwipeEnabled = true,
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                )
            },
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            sheetContent = {
                SessionDetailSheet(
                    state = state.copy(selectedSession = sessionToDisplay),
                    expansionProgress = { sheetController.expansionProgress.value },
                    prevSession = prevSession,
                    nextSession = nextSession,
                    onEvent = viewModel::onEvent,
                    onShowFullScreenImage = { path, bounds ->
                        imageOrigin = bounds
                        viewModel.onEvent(TimelineEvent.ShowFullScreenImage(path))
                    },
                    onBoundsCalculated = onBoundsCalculated,
                    isThumbnailRowVisible = { sheetController.expansionProgress.value < 0.05f }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                TimelineSessionList(
                    sessions = state.sessions,
                    summary = state.summary,
                    isShowingShimmer = isShowingShimmer,
                    navBarPadding = navBarPadding,
                    topPadding = (headerHeight - TopAppBarCutoutRadius).coerceAtLeast(0.dp),
                    onSessionClick = { session -> viewModel.onEvent(TimelineEvent.SelectSession(session)) },
                    onSummaryClick = onNavigateToHighlight,
                    tutorialViewModel = tutorialViewModel,
                    onBoundsCalculated = onBoundsCalculated
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { headerHeight = with(density) { it.height.toDp() } }
                ) {
                    TimelineHeader(
                        selectedDate = state.selectedDate,
                        selectedFilter = state.timeFilter,
                        showTimeFilters = showTimeFilters,
                        onToggleTimeFilters = onToggleTimeFilters,
                        onFilterSelected = { viewModel.onEvent(TimelineEvent.FilterTime(it)) },
                        onNavigateToSettings = onNavigateToSettings,
                        onSelectDateClick = { onShowDatePickerChange(true) },
                        scrollBehavior = scrollBehavior,
                        subscriptionBanner = if (tutorialState.isActive) SubscriptionBanner.None else subscriptionBanner,
                        onProPlanClick = onNavigateToPaywall,
                        onBoundsCalculated = onBoundsCalculated
                    )
                }
            }
        }

        ImageHeroOverlay(
            path = effectiveFullScreenImagePath,
            originBounds = imageOrigin,
            onDismiss = {
                if (tutorialControlsFullScreenImage) {
                    tutorialViewModel.onEvent(
                        TutorialEvent.RealGestureObserved(TutorialGesture.DISMISSED_FULL_SCREEN_IMAGE)
                    )
                } else {
                    viewModel.onEvent(TimelineEvent.DismissFullScreenImage)
                }
            },
            onBoundsCalculated = onBoundsCalculated
        )
    }
}