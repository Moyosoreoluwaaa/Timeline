package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.timeline.domain.Session
import com.timeline.ui.AppIcon
import com.timeline.ui.LocalNavAnimatedVisibilityScope
import com.timeline.ui.LocalSharedTransitionScope
import com.timeline.ui.ScreenshotImage
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionDetailHeader(
    session: Session,
    isExpanded: Boolean,
    totalSessions: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clip(MaterialTheme.shapes.medium)
            .padding(Dimensions.PaddingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "${session.durationMinutes}m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (isExpanded) {
            Text(
                text = "$totalSessions ${if (totalSessions == 1) "session" else "sessions"}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
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
            .navigationBarsPadding() // Keep controls safely above navigation gestures
            .padding(horizontal = Dimensions.PaddingLarge)
            .padding(bottom = Dimensions.PaddingLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        if (prevSession != null) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onPrevClick() }
                    .padding(Dimensions.PaddingSmall)
            ) {
                Text(AppStrings.SessionPrev, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
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
                Text(AppStrings.SessionNext, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
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
    onImageClick: (String?) -> Unit
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val currentProgress = progress()
                alpha = currentProgress
                translationY = (AppAlpha.Full - currentProgress) * Dimensions.SpacingColossal.value
            },
        verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
        contentPadding = PaddingValues(
            start = Dimensions.PaddingMedium,
            end = Dimensions.PaddingMedium,
            top = Dimensions.PaddingSmall,
            bottom = navBarPadding + (Dimensions.SpacingGiant * 2)
        )
    ) {
        items(appSessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.SurfaceVariant)),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                    StatRow(session)
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                    val segmentsWithScreenshots = remember(session) {
                        session.segments.filter { it.screenshotPath != null }
                    }

                    if (segmentsWithScreenshots.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
                            contentPadding = PaddingValues(vertical = Dimensions.Half)
                        ) {
                            items(segmentsWithScreenshots) { segment ->
                                val path = segment.screenshotPath!!
                                val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                    with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            rememberSharedContentState(key = "image-$path"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = { _, _ -> spring(dampingRatio = 0.8f, stiffness = 380f) }
                                        )
                                    }
                                } else Modifier

                                Column(
                                    modifier = Modifier
                                        .width(Dimensions.SpacingMega)
                                        .clickable { onImageClick(path) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = segment.timestamp.formatTime(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(Dimensions.Half))
                                        Text(
                                            text = segment.activityDescription ?: "Activity",
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(AppWeights.Full)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(Dimensions.Half))
                                    ScreenshotImage(
                                        path = path,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(Dimensions.SpacingUltra)
                                            .clip(MaterialTheme.shapes.small)
                                            .then(sharedModifier)
                                    )
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
        StatItem(AppStrings.SessionDuration, "${session.durationMinutes}m")
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

fun kotlin.time.Instant.formatTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour}:${local.minute.toString().padStart(2, '0')}"
}
