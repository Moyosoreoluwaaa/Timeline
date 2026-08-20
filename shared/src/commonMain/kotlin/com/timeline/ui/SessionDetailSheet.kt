package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionDetailSheet(
    session: Session,
    isExpanded: Boolean,
    onEvent: (TimelineEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    // App Icon
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = session.displayName ?: session.packageName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${session.durationMinutes}m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(onClick = { /* Share */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Screenshots Carousel
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) { // Mocking 3 screenshots
                Surface(
                    modifier = Modifier.width(160.dp).height(240.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Session Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("Started", session.startTime.formatTime())
            StatItem("Ended", session.endTime?.formatTime() ?: "--:--")
            StatItem("Duration", "${session.durationMinutes}m")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Earlier Today Segments
        Text("Earlier Today", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(session.segments) { segment ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(segment.timestamp.formatTime(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
                    Text(segment.activityDescription ?: "Active", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (session.segments.isEmpty()) {
                item {
                    Text("Started using ${session.displayName ?: "app"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavButton("Previous", "Slack", true)
            NavButton("Next", "Notion", false)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun NavButton(label: String, appName: String, isBack: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isBack) Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
        Column(horizontalAlignment = if (isBack) Alignment.Start else Alignment.End) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(appName, style = MaterialTheme.typography.bodySmall)
        }
        if (!isBack) Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
    }
}

fun kotlinx.datetime.Instant.formatTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour}:${local.minute.toString().padStart(2, '0')}"
}
