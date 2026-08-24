package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.timeline.domain.Session
import com.timeline.ui.AppIcon
import com.timeline.ui.ScreenshotImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionDetailHeader(
    session: Session,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
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
        IconButton(onClick = onToggleExpanded) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = if (isExpanded) "Toggle Fullscreen" else "Expand"
            )
        }
    }
}

@Composable
fun SessionDetailFooter(
    prevSession: Session?,
    nextSession: Session?,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        if (prevSession != null) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.clickable { onPrevClick() }
            ) {
                Text("Prev", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                AppIcon(
                    icon = prevSession.icon,
                    contentDescription = "Previous",
                    modifier = Modifier.size(34.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (nextSession != null) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.clickable { onNextClick() }
            ) {
                Text("Next", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                AppIcon(
                    icon = nextSession.icon,
                    contentDescription = "Next",
                    modifier = Modifier.size(34.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
fun ExpandedSessionContent(
    currentSession: Session,
    allSessions: List<Session>,
    progress: Float
) {
    val appSessions = remember(currentSession.packageName, allSessions) {
        allSessions.filter { it.packageName == currentSession.packageName }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 100.dp.toPx()
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 120.dp
        )
    ) {
        items(appSessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    StatRow(session)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val segmentsWithScreenshots = remember(session) {
                        session.segments.filter { it.screenshotPath != null }
                    }
                    
                    if (segmentsWithScreenshots.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(segmentsWithScreenshots) { segment ->
                                Column(modifier = Modifier.width(120.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = segment.timestamp.formatTime(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = segment.activityDescription ?: "Activity",
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ScreenshotImage(
                                        path = segment.screenshotPath,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No screenshots available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 8.dp)
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

fun kotlin.time.Instant.formatTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour}:${local.minute.toString().padStart(2, '0')}"
}
