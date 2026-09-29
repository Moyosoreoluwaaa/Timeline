package com.timeline.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.tutorial.SheetLock
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialGesture
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

// How much of the session sheet is visible when collapsed. The thumbnail row
// must fit inside this. ModalBottomSheet used roughly half the screen; tune to taste.
private val SheetPeekHeight = 360.dp

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel(),
    tutorialViewModel: com.timeline.tutorial.TutorialViewModel = koinViewModel(),
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
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val isShowingShimmer = state.isLoading || (tutorialState.isActive && state.sessions.isEmpty())

    // Is the tutorial currently dictating this screen's sheet / full-screen-image
    // state? sheetLock, when non-null, is enforced via the sheet state's
    // confirmValueChange below -- a real drag physically cannot move the sheet
    // to a value the lock forbids, so there is no "fight it back" step.
    val activeSheetLock = if (tutorialState.isActive) tutorialState.sheetLock else null
    val tutorialControlsFullScreenImage =
        tutorialState.isActive && tutorialState.currentStep == TutorialStep.FULL_SCREEN_IMAGE_PREVIEW
    val effectiveFullScreenImagePath = if (tutorialControlsFullScreenImage) {
        tutorialState.requiredFullScreenImagePath
    } else {
        state.fullScreenImagePath
    }

    // ---- Session sheet state (hoisted: the sheet now lives in this window) ----

    val hasSession = state.selectedSession != null

    // Fallback session so sheetContent is always measured with real layout dimensions,
    // ensuring BottomSheetScaffold anchors are computed before sheet is shown.
    val fallbackSession = remember(state.sessions) {
        state.sessions.firstOrNull() ?: com.timeline.domain.Session(
            id = "placeholder",
            packageName = "",
            displayName = "",
            startTime = kotlin.time.Clock.System.now(),
            endTime = kotlin.time.Clock.System.now()
        )
    }

    // Cache the session so the sheet can exit smoothly
    val lastSelectedSession = remember { androidx.compose.runtime.mutableStateOf<com.timeline.domain.Session?>(null) }
    LaunchedEffect(state.selectedSession) {
        if (state.selectedSession != null) {
            lastSelectedSession.value = state.selectedSession
        }
    }
    val sessionToDisplay = state.selectedSession ?: lastSelectedSession.value ?: fallbackSession

    val currentIndex = state.sessions.indexOfFirst { it.id == state.selectedSession?.id }
    val prevSession = if (currentIndex > 0) state.sessions[currentIndex - 1] else null
    val nextSession =
        if (currentIndex != -1 && currentIndex < state.sessions.lastIndex) state.sessions[currentIndex + 1] else null

    // confirmValueChange is Material3's veto hook: it runs BEFORE a drag/settle
    // is committed and can reject it. We read the lock through rememberUpdatedState
    // so the lambda instance stays stable -- otherwise a new lambda every time the
    // lock changes would make rememberSaveable rebuild (reset) the sheet state.
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

    // expansionProgress tracks the REAL sheet state.
    val isSheetActuallyExpanded = sheetState.currentValue == SheetValue.Expanded
    val expansionProgress by animateFloatAsState(
        targetValue = if (isSheetActuallyExpanded) 1f else 0f,
        label = "ExpansionProgress"
    )

    // Single source of truth for target sheet state
    val targetSheetValue = when {
        !hasSession -> SheetValue.Hidden
        activeSheetLock is SheetLock.Fixed -> if (activeSheetLock.expanded) SheetValue.Expanded else SheetValue.PartiallyExpanded
        activeSheetLock is SheetLock.Free -> if (activeSheetLock.startExpanded) SheetValue.Expanded else SheetValue.PartiallyExpanded
        state.isSheetExpanded -> SheetValue.Expanded
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

    // Real sheet movements -> reported outward.
    // - No active lock: write straight through to the real TimelineViewModel.
    // - Free lock (EXPAND_BOTTOM_SHEET only): a real drag to the far value is
    //   user progress -- report it as a gesture so the tutorial advances.
    // - Fixed lock: the sheet cannot leave the pinned value (vetoed above), so
    //   there is nothing to report; taps on prev/next advance that step via
    //   realInteractions in AppRootContainer.
    LaunchedEffect(sheetState.currentValue) {
        val isExpanded = sheetState.currentValue == SheetValue.Expanded
        when (val lock = activeSheetLock) {
            null -> {
                if (isExpanded != state.isSheetExpanded) {
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
            is SheetLock.Fixed -> { /* pinned; nothing to report */ }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

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

        // NOTE: DatePickerDialog is still its own window, so the tutorial overlay
        // cannot draw above it (affects SPOTLIGHT_DATE_PICKER only).
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

            val dragHandle: @Composable () -> Unit = { BottomSheetDefaults.DragHandle() }

            // The session sheet now lives in THIS window (BottomSheetScaffold),
            // not in a separate ModalBottomSheet window -- so the tutorial
            // overlay, which is drawn later in the same window, can sit above it.
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                modifier = Modifier
                    .padding(top = topPadding)
                    .fillMaxSize(),
                sheetPeekHeight = SheetPeekHeight,
                sheetSwipeEnabled = true,
                sheetDragHandle = dragHandle,
                sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                sheetContent = {
                    SessionDetailSheet(
                        state = state.copy(selectedSession = sessionToDisplay),
                        expansionProgress = { expansionProgress },
                        prevSession = prevSession,
                        nextSession = nextSession,
                        onEvent = viewModel::onEvent,
                        onShowFullScreenImage = { path ->
                            viewModel.onEvent(TimelineEvent.ShowFullScreenImage(path))
                        },
                        onBoundsCalculated = onBoundsCalculated,
                        // Thumbnail row only reports bounds when it is actually
                        // the collapsed, opaque, visible target -- not mid-fade
                        // as the sheet expands over it.
                        isThumbnailRowVisible = { expansionProgress < 0.05f },
                        isSheetExpandedOverride = if (activeSheetLock != null) isSheetActuallyExpanded else null
                    )
                }
            ) { _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (state.sessions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(AppWeights.Full)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isShowingShimmer) {
                                        val alpha by androidx.compose.animation.core.rememberInfiniteTransition(label = "ShimmerTransition").animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 0.9f,
                                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                animation = androidx.compose.animation.core.tween(durationMillis = 1000),
                                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                            ),
                                            label = "ShimmerAlpha"
                                        )
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            userScrollEnabled = false,
                                            contentPadding = PaddingValues(
                                                top = Dimensions.PaddingSmall,
                                                bottom = navBarPadding + (Dimensions.PaddingLarge * 2)
                                            )
                                        ) {
                                            items(5) { index ->
                                                Box(modifier = Modifier.alpha(alpha)) {
                                                    TimelineEntry(
                                                        session = com.timeline.domain.Session(
                                                            id = "shimmer_$index",
                                                            packageName = "com.placeholder.app",
                                                            displayName = "Loading Activity...",
                                                            startTime = kotlin.time.Clock.System.now(),
                                                            endTime = kotlin.time.Clock.System.now(),
                                                            durationMinutes = 10
                                                        ),
                                                        isFirst = index == 0,
                                                        isLast = index == 4,
                                                        onClick = {}
                                                    )
                                                }
                                            }
                                        }
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
                                ),
                                tutorialViewModel = tutorialViewModel
                            )
                        }
                    }
                }
            }
        }

        // Full-screen image preview. This is a plain in-window layer (NOT a
        // Dialog) so the tutorial overlay, drawn later in the same window,
        // renders on top of it. zIndex keeps it above the scaffold + sheet.
        val imagePath = effectiveFullScreenImagePath
        if (imagePath != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (tutorialControlsFullScreenImage) {
                            tutorialViewModel.onEvent(
                                TutorialEvent.RealGestureObserved(TutorialGesture.DISMISSED_FULL_SCREEN_IMAGE)
                            )
                        } else {
                            viewModel.onEvent(TimelineEvent.DismissFullScreenImage)
                        }
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