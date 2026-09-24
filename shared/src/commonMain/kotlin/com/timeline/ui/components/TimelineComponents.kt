package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.timeline.domain.Session
import com.timeline.presentation.TimelineSummary
import com.timeline.ui.AppIcon
import com.timeline.ui.ScreenshotImage
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import com.timeline.util.TimeFormatter

@Composable
fun TimelineEntry(
    session: Session,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = Dimensions.PaddingMedium)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val timeText = remember(session.startTime) {
            TimeFormatter.formatTime(session.startTime)
        }
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(Dimensions.PaddingLarge * 2)
        )

        Column(
            modifier = Modifier.fillMaxHeight().width(Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(Dimensions.LineThickness)
                    .weight(AppWeights.Full)
                    .background(if (isFirst) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            )
            Surface(
                modifier = Modifier.size(Dimensions.PaddingSmall),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
            Box(
                modifier = Modifier
                    .width(Dimensions.LineThickness)
                    .weight(AppWeights.Full)
                    .background(if (isLast) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            )
        }

        // Replaced Card with Box + clip(shape) so ripple effect strictly stays within rounded corners
        Box(
            modifier = Modifier
                .weight(AppWeights.Full)
                .padding(vertical = Dimensions.PaddingSmall)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Divider))
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(Dimensions.PaddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(Dimensions.IconLarge),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AppIcon(
                        icon = session.icon,
                        contentDescription = session.displayName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
                Column(modifier = Modifier.weight(AppWeights.Full)) {
                    val displayName = session.displayName ?: session.packageName.split(".").last().replaceFirstChar { it.uppercase() }
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatDuration(session.durationMinutes, session.durationSeconds),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun formatDuration(minutes: Int, seconds: Int): String {
    val totalMinutes = minutes + (seconds / 60)
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
fun FullScreenImageOverlay(
    path: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = path != null,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = Modifier.fillMaxSize().zIndex(100f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            ScreenshotImage(
                path = path,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(0.95f)
                    .clip(MaterialTheme.shapes.large)
            )
        }
    }
}

@Composable
fun BottomSummary(
    summary: TimelineSummary?,
    onSummaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (summary == null) return

    Surface(
        modifier = modifier
            .padding(Dimensions.PaddingMedium)
            .navigationBarsPadding()
            .fillMaxWidth(),
        shape = com.timeline.ui.theme.AppShapes.Pill,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimensions.ModalElevation
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onSummaryClick() }
                .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(Dimensions.Half)
            ) {
                Text(
                    text = AppStrings.TimelineTotalUsage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${summary.totalHours}h ${summary.totalMinutes}m",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(Dimensions.Half)
            ) {
                Text(
                    text = AppStrings.TimelineSessionsCount,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = summary.sessionCount.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(Dimensions.Half)
            ) {
                Text(
                    text = AppStrings.TimelineMostUsed,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Half)) {
                    summary.mostUsedApps.forEach { app ->
                        AppIcon(
                            icon = app.icon,
                            contentDescription = app.displayName,
                            modifier = Modifier.size(Dimensions.IconMedium)
                        )
                    }
                }
            }
        }
    }
}