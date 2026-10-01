package com.timeline.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
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
import com.timeline.ui.components.SubscriptionBanner
import com.timeline.ui.components.TimelineEntry
import com.timeline.ui.components.TimelineHeader
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.empty_state_illustration
import kotlin.time.Instant

// How much of the session sheet is visible when collapsed. The thumbnail row
// must fit inside this. ModalBottomSheet used roughly half the screen; tune to taste.
private val SheetPeekHeight = 400.dp

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

    val hasSession = state.selectedSession != null

    val fallbackSession = remember(state.sessions) {
        state.sessions.firstOrNull() ?: com.timeline.domain.Session(
            id = "placeholder",
            packageName = "",
            displayName = "",
            startTime = kotlin.time.Clock.System.now(),
            endTime = kotlin.time.Clock.System.now()
        )
    }

    val lastSelectedSession =
        remember { androidx.compose.runtime.mutableStateOf<com.timeline.domain.Session?>(null) }
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
    val expansionProgress by animateFloatAsState(
        targetValue = if (isSheetActuallyExpanded) 1f else 0f,
        label = "ExpansionProgress"
    )

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

            is SheetLock.Fixed -> { /* pinned; nothing to report */
            }
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
                    // Hidden during the tutorial so it doesn't shift spotlight bounds
                    subscriptionBanner = if (tutorialState.isActive) SubscriptionBanner.None else subscriptionBanner,
                    onProPlanClick = onNavigateToPaywall,
                    onBoundsCalculated = onBoundsCalculated
                )
            }
        ) { padding ->
            val navBarPadding =
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val topPadding =
                (padding.calculateTopPadding() - TopAppBarCutoutRadius).coerceAtLeast(0.dp)

            val dragHandle: @Composable () -> Unit = { BottomSheetDefaults.DragHandle() }

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
                                        val alpha by androidx.compose.animation.core.rememberInfiniteTransition(
                                            label = "ShimmerTransition"
                                        ).animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 0.9f,
                                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                animation = androidx.compose.animation.core.tween(
                                                    durationMillis = 2000
                                                ),
                                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                            ),
                                            label = "ShimmerAlpha"
                                        )
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
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
                                                            packageName = "" +
//                                                                    "com.placeholder.app" +
                                                                    "",
                                                            displayName = "",
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
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(Dimensions.PaddingMedium),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Image(
                                                painter = painterResource(Res.drawable.empty_state_illustration),
                                                contentDescription = "Empty activity state",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .padding(bottom = Dimensions.PaddingSmall)
                                            )

                                            // Formats text so it wraps to the next line right after the comma following the 4th word
                                            val formattedEmptyText =
                                                AppStrings.TimelineNoActivity.let { text ->
                                                    val words = text.split(" ")
                                                    if (words.size >= 7 && words[6].endsWith(",")) {
                                                        words.take(7)
                                                            .joinToString(" ") + "\n" + words.drop(4)
                                                            .joinToString(" ")
                                                    } else {
                                                        text.replace(", ", ",\n")
                                                    }
                                                }

                                            Text(
                                                text = formattedEmptyText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
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

        if (effectiveFullScreenImagePath != null) {
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
                    path = effectiveFullScreenImagePath,
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