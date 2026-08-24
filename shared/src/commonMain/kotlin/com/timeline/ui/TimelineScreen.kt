package com.timeline.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineViewModel
import com.timeline.ui.components.*
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {}
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
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TimelineHeader(
                selectedDate = state.selectedDate,
                onToggleTimeFilters = { showTimeFilters = !showTimeFilters },
                onNavigateToSettings = onNavigateToSettings,
                onSelectDateClick = { showDatePicker = true }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (showTimeFilters) {
                    TimelineFilterSection(
                        selectedFilter = state.timeFilter,
                        onFilterSelected = { viewModel.onEvent(TimelineEvent.FilterTime(it)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.sessions.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No activity recorded", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(state.sessions.size) { index ->
                            val session = state.sessions[index]
                            TimelineEntry(
                                session = session,
                                isFirst = index == 0,
                                isLast = index == state.sessions.lastIndex
                            ) { viewModel.onEvent(TimelineEvent.SelectSession(session)) }
                        }
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomSummary(state.sessions)
            }
        }

        if (state.selectedSession != null) {
            val currentIndex = state.sessions.indexOfFirst { it.id == state.selectedSession?.id }
            val prevSession = if (currentIndex > 0) state.sessions[currentIndex - 1] else null
            val nextSession = if (currentIndex != -1 && currentIndex < state.sessions.lastIndex) state.sessions[currentIndex + 1] else null

            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(TimelineEvent.SelectSession(null)) },
                sheetState = sheetState
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
