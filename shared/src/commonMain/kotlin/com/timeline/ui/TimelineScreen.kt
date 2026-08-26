package com.timeline.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.ui.components.*
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val expansionProgress by animateFloatAsState(
        targetValue = if (sheetState.targetValue == SheetValue.Expanded) 1f else 0f,
        label = "ExpansionProgress"
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimeFilters by remember { mutableStateOf(false) }

    LaunchedEffect(sheetState.currentValue) {
        val isAtTop = sheetState.currentValue == SheetValue.Expanded
        if (isAtTop != state.isSheetExpanded) {
            viewModel.onEvent(TimelineEvent.ToggleSheet(isAtTop))
        }
    }

    LaunchedEffect(state.isSheetExpanded) {
        if (state.isSheetExpanded && sheetState.currentValue != SheetValue.Expanded) {
            sheetState.expand()
        } else if (!state.isSheetExpanded && sheetState.currentValue == SheetValue.Expanded) {
            sheetState.partialExpand()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
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

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars, // Applies status bar inset top-padding only
        topBar = {
            TimelineHeader(
                selectedDate = state.selectedDate,
                selectedFilter = state.timeFilter,
                showTimeFilters = showTimeFilters,
                onToggleTimeFilters = { showTimeFilters = !showTimeFilters },
                onFilterSelected = { viewModel.onEvent(TimelineEvent.FilterTime(it)) },
                onNavigateToSettings = onNavigateToSettings,
                onSelectDateClick = { showDatePicker = true }
            )
        }
    ) { padding ->
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = Dimensions.SpacingLarge, topEnd = Dimensions.SpacingLarge)),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
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
                                Text(
                                    AppStrings.TimelineNoActivity,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(AppWeights.Full)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    bottom = navBarPadding + (Dimensions.SpacingGiant * 2)
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
                            .navigationBarsPadding() // Ensures bottom summary bar floats above system nav gestures
                    ) {
                        BottomSummary(
                            sessions = state.sessions,
                            onUpgradeClick = onNavigateToPaywall
                        )
                    }
                }
            }
        }

        if (state.selectedSession != null) {
            val currentIndex = state.sessions.indexOfFirst { it.id == state.selectedSession?.id }
            val prevSession = if (currentIndex > 0) state.sessions[currentIndex - 1] else null
            val nextSession = if (currentIndex != -1 && currentIndex < state.sessions.lastIndex) state.sessions[currentIndex + 1] else null

            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(TimelineEvent.SelectSession(null)) },
                sheetState = sheetState,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) } // Prevents sheet from injecting dynamic bottom inset padding
            ) {
                SessionDetailSheet(
                    session = state.selectedSession!!,
                    allSessions = state.sessions,
                    isExpanded = state.isSheetExpanded,
                    expansionProgress = expansionProgress,
                    prevSession = prevSession,
                    nextSession = nextSession,
                    onEvent = viewModel::onEvent
                )
            }
        }

    }
}
