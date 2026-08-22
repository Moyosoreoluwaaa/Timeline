package com.timeline.ui

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
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionDetailSheet(
    session: Session,
    allSessions: List<Session>,
    isExpanded: Boolean,
    expansionProgress: Float, // 0f to 1f
    prevSession: Session?,
    nextSession: Session?,
    onEvent: (TimelineEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header - Padding moved here from root to prevent "window" clipping at the bottom
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
                IconButton(onClick = { onEvent(TimelineEvent.ToggleSheet(!isExpanded)) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isExpanded) "Toggle Fullscreen" else "Expand"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                // Summary view (Stats + Screenshots)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 80.dp) // Avoid overlap with floating footer
                        .graphicsLayer {
                            alpha = 1f - expansionProgress
                            translationY = -expansionProgress * 50.dp.toPx()
                        }
                ) {
                    StatRow(session)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show all segments with screenshots
                        val segmentsWithScreenshots = session.segments.filter { it.screenshotPath != null }
                        items(segmentsWithScreenshots) { segment ->
                            ScreenshotImage(
                                path = segment.screenshotPath,
                                contentDescription = "Session segment",
                                modifier = Modifier.width(120.dp).height(180.dp)
                            )
                        }
                    }
                }

                // Expanded content - fades in and slides up
                ExpandedSessionContent(
                    currentSession = session,
                    allSessions = allSessions,
                    progress = expansionProgress
                )
            }
        }

        // Navigation Footer - Floating on top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding() // Keep the footer safe from the system navigation bar
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp), // Extra spacing from bottom
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (prevSession != null) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.clickable { onEvent(TimelineEvent.SelectPreviousSession) }
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
                    modifier = Modifier.clickable { onEvent(TimelineEvent.SelectNextSession) }
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
            bottom = 120.dp // Extra space for floating footer and nav bar
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
