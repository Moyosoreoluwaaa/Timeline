package com.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.ui.components.BottomSummary
import com.timeline.ui.components.TimelineEntry
import com.timeline.ui.components.TimelineHeader
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHighlight: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val density = LocalDensity.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimeFilters by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullHeight = constraints.maxHeight.toFloat()
        val peekHeight = fullHeight * 0.55f
        val expandedHeight = 0f

        val anchors = remember(fullHeight) {
            DraggableAnchors {
                SheetValue.Hidden at fullHeight
                SheetValue.PartiallyExpanded at peekHeight
                SheetValue.Expanded at expandedHeight
            }
        }

        val velocityThresholdPx = with(density) { 100.dp.toPx() }
        val sheetState = remember {
            AnchoredDraggableState(
                initialValue = SheetValue.Hidden,
                positionalThreshold = { distance -> distance * 0.5f },
                velocityThreshold = { velocityThresholdPx },
                snapAnimationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
                decayAnimationSpec = exponentialDecay()
            )
        }

        LaunchedEffect(anchors) {
            sheetState.updateAnchors(anchors)
        }

        val expansionProgressProvider = remember {
            derivedStateOf {
                val currentAnchors = sheetState.anchors
                if (currentAnchors.size > 1) {
                    val peekPos = currentAnchors.positionOf(SheetValue.PartiallyExpanded)
                    val expandedPos = currentAnchors.positionOf(SheetValue.Expanded)
                    if (!peekPos.isNaN() && !expandedPos.isNaN() && (expandedPos - peekPos) != 0f) {
                        ((sheetState.offset - peekPos) / (expandedPos - peekPos)).coerceIn(0f, 1f)
                    } else 0f
                } else 0f
            }
        }

        LaunchedEffect(state.selectedSession) {
            if (state.selectedSession != null) {
                if (sheetState.currentValue == SheetValue.Hidden) {
                    sheetState.animateTo(SheetValue.PartiallyExpanded)
                }
            } else {
                sheetState.animateTo(SheetValue.Hidden)
            }
        }

        LaunchedEffect(state.isSheetExpanded) {
            if (state.isSheetExpanded && sheetState.currentValue != SheetValue.Expanded) {
                sheetState.animateTo(SheetValue.Expanded)
            } else if (!state.isSheetExpanded && sheetState.currentValue == SheetValue.Expanded) {
                sheetState.animateTo(SheetValue.PartiallyExpanded)
            }
        }

        LaunchedEffect(sheetState.currentValue) {
            val isExpanded = sheetState.currentValue == SheetValue.Expanded
            if (isExpanded != state.isSheetExpanded) {
                viewModel.onEvent(TimelineEvent.ToggleSheet(isExpanded))
            }
            if (sheetState.currentValue == SheetValue.Hidden && state.selectedSession != null) {
                viewModel.onEvent(TimelineEvent.SelectSession(null))
            }
        }

        BackHandler(enabled = state.selectedSession != null || state.fullScreenImagePath != null) {
            if (state.fullScreenImagePath != null) {
                viewModel.onEvent(TimelineEvent.DismissFullScreenImage)
            } else if (state.isSheetExpanded) {
                viewModel.onEvent(TimelineEvent.ToggleSheet(false))
            } else {
                viewModel.onEvent(TimelineEvent.SelectSession(null))
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.selectedDate?.toEpochMilliseconds()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEvent(TimelineEvent.SelectDate(Instant.fromEpochMilliseconds(it)))
                        }
                        showDatePicker = false
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
                    onToggleTimeFilters = { showTimeFilters = !showTimeFilters },
                    onFilterSelected = { viewModel.onEvent(TimelineEvent.FilterTime(it)) },
                    onNavigateToSettings = onNavigateToSettings,
                    onSelectDateClick = { showDatePicker = true },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val topPadding = (padding.calculateTopPadding() - TopAppBarCutoutRadius).coerceAtLeast(0.dp)

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
                                    TimelineEntry(
                                        session = session,
                                        isFirst = index == 0,
                                        isLast = index == state.sessions.lastIndex,
                                        modifier = Modifier.animateItem()
                                    ) { viewModel.onEvent(TimelineEvent.SelectSession(session)) }
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    ) {
                        BottomSummary(
                            summary = state.summary,
                            onSummaryClick = onNavigateToHighlight
                        )
                    }
                }
            }
        }

        // Scrim for sheet
        if (state.selectedSession != null) {
            val scrimAlpha by animateFloatAsState(
                targetValue = if (sheetState.targetValue != SheetValue.Hidden) 0.6f else 0f,
                label = "ScrimAlpha"
            )
            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.onEvent(TimelineEvent.SelectSession(null))
                        }
                        .zIndex(10f)
                )
            }
        }

        // Custom Draggable Sheet
        val cornerRadius by animateDpAsState(
            targetValue = if (expansionProgressProvider.value > 0.9f) 0.dp else 28.dp,
            label = "SheetCornerRadius"
        )

        Box(
            modifier = Modifier
                .offset {
                    val y = if (sheetState.anchors.size > 0) sheetState.requireOffset() else fullHeight
                    IntOffset(0, y.roundToInt())
                }
                .anchoredDraggable(sheetState, Orientation.Vertical)
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .zIndex(11f)
        ) {
            if (state.selectedSession != null) {
                val currentIndex = state.sessions.indexOfFirst { it.id == state.selectedSession?.id }
                val prevSession = if (currentIndex > 0) state.sessions[currentIndex - 1] else null
                val nextSession = if (currentIndex != -1 && currentIndex < state.sessions.lastIndex) state.sessions[currentIndex + 1] else null

                SessionDetailSheet(
                    state = state,
                    expansionProgress = { expansionProgressProvider.value },
                    prevSession = prevSession,
                    nextSession = nextSession,
                    onEvent = viewModel::onEvent,
                    onShowFullScreenImage = { path ->
                        viewModel.onEvent(TimelineEvent.ShowFullScreenImage(path))
                    }
                )
            }
        }

        // Full-Screen Image Overlay
        val imagePath = state.fullScreenImagePath
        AnimatedVisibility(
            visible = imagePath != null,
            enter = fadeIn(spring(stiffness = 500f)) + scaleIn(spring(dampingRatio = 0.8f, stiffness = 400f), initialScale = 0.92f),
            exit = fadeOut(spring(stiffness = 500f)) + scaleOut(spring(dampingRatio = 0.8f, stiffness = 400f), targetScale = 0.92f),
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            if (imagePath != null && sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
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
                                .sharedElement(
                                    rememberSharedContentState(key = "image-$imagePath"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ -> spring(dampingRatio = 0.8f, stiffness = 380f) }
                                )
                        )
                    }
                }
            }
        }
    }
}