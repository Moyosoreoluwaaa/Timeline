package com.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.SetupEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.SessionDetailSheet
import com.timeline.ui.SetupScreen
import com.timeline.ui.SettingsScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock as KClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: TimelineViewModel = koinViewModel(),
    setupViewModel: SetupViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateToUsageStats: () -> Unit = {},
    onNavigateToOverlay: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val setupState by setupViewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    
    var showSettings by remember { mutableStateOf(false) }

    MaterialTheme {
        if (!setupState.isUsageStatsGranted || !setupState.isOverlayGranted || !setupState.isNotificationGranted) {
            SetupScreen(
                viewModel = setupViewModel,
                onNavigateToUsageStats = onNavigateToUsageStats,
                onNavigateToOverlay = onNavigateToOverlay,
                onNavigateToNotification = onNavigateToNotification,
                onComplete = { setupViewModel.onEvent(SetupEvent.CheckPermissions) }
            )
        } else if (showSettings) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { showSettings = false }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Timeline") },
                        actions = {
                            IconButton(onClick = { /* Toggle time filters */ }) {
                                Icon(Icons.Default.DateRange, "Time of Day")
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    // Date Selector
                    val dateText = remember(state.selectedDate) {
                        val date = state.selectedDate ?: KClock.System.now()
                        val local = date.toLocalDateTime(TimeZone.currentSystemDefault())
                        "${local.month.name} ${local.day}, ${local.year}"
                    }

                    Text(
                        "Today ↓  $dateText",
                        modifier = Modifier.padding(16.dp).clickable { /* Open Calendar */ },
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (state.sessions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No activity recorded yet", style = MaterialTheme.typography.bodyLarge)
                                Text("Tracking service is active in background", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.onEvent(TimelineEvent.Refresh) }) {
                                    Text("Refresh")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.onEvent(TimelineEvent.GenerateDummyData) }) {
                                    Text("Generate Dummy Data")
                                }
                            }
                        }
                    } else {
                        // Timeline List
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.sessions) { session ->
                                TimelineEntry(session) {
                                    viewModel.onEvent(TimelineEvent.SelectSession(session))
                                }
                            }
                        }
                    }
                }

                if (state.selectedSession != null) {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.onEvent(TimelineEvent.SelectSession(null)) },
                        sheetState = sheetState
                    ) {
                        SessionDetailSheet(
                            session = state.selectedSession!!,
                            isExpanded = state.isSheetExpanded,
                            onEvent = viewModel::onEvent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEntry(session: com.timeline.domain.Session, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("◉", color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(session.packageName, style = MaterialTheme.typography.titleMedium)
                Text("${session.startTime} · ${session.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
