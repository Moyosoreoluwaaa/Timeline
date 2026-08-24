package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimeFilter
import com.timeline.ui.AppIcon
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineHeader(
    selectedDate: Instant?,
    onToggleTimeFilters: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectDateClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text("Timeline", style = MaterialTheme.typography.headlineMedium)
                Row(
                    modifier = Modifier.clickable { onSelectDateClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    val dateText = remember(selectedDate) {
                        val date = selectedDate ?: Clock.System.now()
                        val local = date.toLocalDateTime(TimeZone.currentSystemDefault())
                        "${local.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${local.day}, ${local.year}"
                    }
                    Text(dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        actions = {
            IconButton(onClick = onToggleTimeFilters) {
                Icon(Icons.Default.DateRange, "Time of Day")
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, "Settings")
            }
        }
    )
}

@Composable
fun TimelineFilterSection(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(TimeFilter.entries.size) { index ->
            val filter = TimeFilter.entries[index]
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@Composable
fun TimelineEntry(
    session: Session,
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
                    AppIcon(
                        icon = session.icon,
                        contentDescription = session.displayName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayName = session.displayName ?: session.packageName.split(".").last().replaceFirstChar { it.uppercase() }
                    Text(
                        text = displayName,
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
fun BottomSummary(sessions: List<Session>) {
    val totalMinutes = sessions.sumOf { it.durationMinutes }
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .navigationBarsPadding()
            .fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total usage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("${hours}h ${minutes}m", style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(sessions.size.toString(), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Most used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sessions.take(3).forEach { session ->
                        AppIcon(
                            icon = session.icon,
                            contentDescription = session.displayName,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
