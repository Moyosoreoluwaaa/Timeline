package com.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timeline.presentation.TimelineViewModel
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.SetupEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.presentation.TimeFilter
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

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                            title = { Text("Everett", style = MaterialTheme.typography.headlineMedium) },
                            actions = {
                                IconButton(onClick = { /* Toggle calendar */ }) {
                                    Icon(Icons.Default.DateRange, "Calendar")
                                }
                                IconButton(onClick = { showSettings = true }) {
                                    Icon(Icons.Default.Settings, "Settings")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .fillMaxSize()
                    ) {
                        // Date Selector
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Today ↓", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateText = remember(state.selectedDate) {
                                val date = state.selectedDate ?: KClock.System.now()
                                val local = date.toLocalDateTime(TimeZone.currentSystemDefault())
                                "${local.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${local.dayOfMonth}, ${local.year}"
                            }
                            Text(dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }

                        // Time Filters
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeFilter.entries.forEach { filter ->
                                FilterChip(
                                    selected = state.timeFilter == filter,
                                    onClick = { viewModel.onEvent(TimelineEvent.FilterTime(filter)) },
                                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.sessions.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No activity recorded", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { viewModel.onEvent(TimelineEvent.GenerateDummyData) }) {
                                        Text("Generate Dummy Data")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                itemsIndexed(state.sessions) { index, session ->
                                    TimelineEntry(
                                        session = session,
                                        isFirst = index == 0,
                                        isLast = index == state.sessions.lastIndex
                                    ) {
                                        viewModel.onEvent(TimelineEvent.SelectSession(session))
                                    }
                                }
                            }
                        }

                        // Bottom Summary as per mockup
                        BottomSummary(state.sessions)
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
}

@Composable
fun TimelineEntry(
    session: com.timeline.domain.Session,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time
        val timeText = remember(session.startTime) {
            val local = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
            "${local.hour}:${local.minute.toString().padStart(2, '0')}"
        }
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(40.dp)
        )

        // Line and Dot
        Column(
            modifier = Modifier.fillMaxHeight().width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .weight(1f)
                    .background(if (isFirst) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            )
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            )
        }

        // Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    // Placeholder for Icon
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.displayName ?: session.packageName.split(".").last().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = "${session.durationMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun BottomSummary(sessions: List<com.timeline.domain.Session>) {
    val totalMinutes = sessions.sumOf { it.durationMinutes }
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total usage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("${hours}h ${minutes}m", style = MaterialTheme.typography.titleMedium)
        }
        Column {
            Text("Sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text("${sessions.size}", style = MaterialTheme.typography.titleMedium)
        }
        Column {
            Text("Most used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Mock icons
                repeat(minOf(sessions.size, 3)) {
                    Surface(modifier = Modifier.size(20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {}
                }
            }
        }
    }
}
