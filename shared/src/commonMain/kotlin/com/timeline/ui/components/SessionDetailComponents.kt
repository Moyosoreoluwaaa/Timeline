package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.timeline.domain.Session
import com.timeline.ui.AppIcon
import com.timeline.ui.ScreenshotImage
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionDetailHeader(
    session: Session,
    expansionProgress: () -> Float,
    relatedSessions: List<Session>
) {
    val totalSessions = relatedSessions.size
    val totalSeconds = relatedSessions.sumOf { it.durationMinutes * 60 + it.durationSeconds }
    val totalDurationFormatted = formatDuration(totalSeconds / 60, totalSeconds % 60)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = Dimensions.PaddingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(Dimensions.IconLarge),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AppIcon(
                    icon = session.icon,
                    contentDescription = session.displayName,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
            Column {
                Text(
                    text = session.displayName ?: session.packageName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = totalDurationFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "$totalSessions ${if (totalSessions == 1) "session" else "sessions"}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer { alpha = expansionProgress() }
        )
    }
}

@Composable
fun SessionDetailFooter(
    prevSession: Session?,
    nextSession: Session?,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onBoundsCalculated: ((Rect) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimensions.PaddingLarge)
            .padding(bottom = Dimensions.PaddingLarge)
            .onGloballyPositioned { coordinates ->
                onBoundsCalculated?.invoke(coordinates.boundsInRoot())
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        if (prevSession != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onPrevClick() }
                    .padding(Dimensions.PaddingSmall)
            ) {
                Text(
                    AppStrings.SessionPrev,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Dimensions.Half))
                AppIcon(
                    icon = prevSession.icon,
                    contentDescription = AppStrings.ContentDescPrevSession,
                    modifier = Modifier.size(Dimensions.IconLarge)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(Dimensions.SpacingHuge))
        }

        if (nextSession != null) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onNextClick() }
                    .padding(Dimensions.PaddingSmall)
            ) {
                Text(
                    AppStrings.SessionNext,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Dimensions.Half))
                AppIcon(
                    icon = nextSession.icon,
                    contentDescription = AppStrings.ContentDescNextSession,
                    modifier = Modifier.size(Dimensions.IconLarge)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(Dimensions.SpacingHuge))
        }
    }
}

@Composable
fun ExpandedSessionContent(
    appSessions: List<Session>,
    progress: () -> Float,
    interactive: Boolean,
    onImageClick: (String, Rect) -> Unit
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        userScrollEnabled = interactive,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = progress()
                // Fades in over the last 70% of the drag (overlapping the collapsed
                // row's fade-out) while settling up into place
                alpha = ((p - 0.3f) / 0.7f).coerceIn(0f, 1f)
                translationY = (1f - p) * -Dimensions.PaddingSmall.toPx()
            }
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
        contentPadding = PaddingValues(
            start = Dimensions.PaddingMedium,
            end = Dimensions.PaddingMedium,
            // The sheet header already sits below the drag handle (which carries the
            // status-bar inset), so no extra status-bar padding here.
            top = Dimensions.PaddingSmall,
            bottom = navBarPadding + (Dimensions.SpacingGiant * 2)
        )
    ) {
        items(appSessions, key = { it.id }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = AppAlpha.SurfaceVariant
                    )
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                    StatRow(session)
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                    val uniqueSegments = remember(session.segments) {
                        session.segments
                            .filter { it.screenshotPath != null }
                            .distinctBy { it.screenshotPath }
                    }

                    if (uniqueSegments.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
//                            contentPadding = PaddingValues(vertical = Dimensions.Half)
                        ) {
                            items(uniqueSegments, key = { it.screenshotPath!! }) { segment ->
                                val path = segment.screenshotPath!!
                                Column(
                                    modifier = Modifier
                                        .width(Dimensions.SpacingMega)
                                        .clickableWithBounds(enabled = interactive) {
                                            onImageClick(
                                                path,
                                                it
                                            )
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(Dimensions.SpacingUltra)
                                            .clip(MaterialTheme.shapes.small)
                                    ) {
                                        ScreenshotImage(
                                            path = path,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(1.35f), // Keyboard crop
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )

//                                        // Overlay label: capture time only
//                                        Surface(
//                                            modifier = Modifier
//                                                .align(Alignment.BottomStart)
//                                                .fillMaxWidth(),
//                                            color = Color.Transparent
//                                        ) {
//                                            Box(
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
//                                                contentAlignment = Alignment.BottomStart
//                                            ) {
//                                                Text(
//                                                    text = segment.timestamp.formatTime(),
//                                                    style = MaterialTheme.typography.labelSmall,
//                                                    fontWeight = FontWeight.Bold,
//                                                    color = AppColors.BrandYellow
//                                                )
//                                            }
//                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = AppStrings.SessionNoScreenshots,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = Dimensions.PaddingSmall)
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
        StatItem(AppStrings.SessionStarted, session.startTime.formatTime())
        StatItem(AppStrings.SessionEnded, session.endTime?.formatTime() ?: "--:--")
        StatItem(
            AppStrings.SessionDuration,
            formatDuration(session.durationMinutes, session.durationSeconds)
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

fun kotlin.time.Instant.formatTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = local.hour
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"
    val minute = local.minute.toString().padStart(2, '0')
    return "$displayHour:$minute $amPm"
}