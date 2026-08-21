package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    prevAppName: String?,
    nextAppName: String?,
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
                    AppIcon(
                        icon = session.icon,
                        contentDescription = session.displayName,
                        modifier = Modifier.fillMaxSize()
                    )
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
            IconButton(onClick = { onEvent(TimelineEvent.ToggleSheet(!isExpanded)) }) {
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isExpanded) {
            ExpandedSessionContent(session)
        } else {
            Column {
                StatRow(session)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(session.segments.take(3)) { segment ->
                        ScreenshotImage(
                            path = segment.screenshotPath,
                            contentDescription = "Session segment",
                            modifier = Modifier.width(100.dp).height(150.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (prevAppName != null) {
                NavButton("Previous", prevAppName, true) {
                    onEvent(TimelineEvent.SelectPreviousSession)
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }

            if (nextAppName != null) {
                NavButton("Next", nextAppName, false) {
                    onEvent(TimelineEvent.SelectNextSession)
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
    }
}

@Composable
fun ExpandedSessionContent(session: Session) {
    Column {
        StatRow(session)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Application Timeline", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(session.segments) { segment ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = segment.timestamp.formatTime(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            text = segment.activityDescription ?: "Active session segment",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (segment.screenshotPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ScreenshotImage(
                            path = segment.screenshotPath,
                            contentDescription = "Segment screenshot",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(session: Session) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem("Started", session.startTime.formatTime())
        StatItem("Ended", session.endTime?.formatTime() ?: "--:--")
        StatItem("Duration", "${session.durationMinutes}m")
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
fun NavButton(label: String, appName: String, isBack: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
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
