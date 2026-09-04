package com.timeline.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.timeline.domain.Session
import com.timeline.presentation.TimeFilter
import com.timeline.presentation.TimelineSummary
import com.timeline.ui.AppIcon
import com.timeline.ui.ScreenshotImage
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import com.timeline.util.TimeFormatter
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineHeader(
    selectedDate: Instant?,
    selectedFilter: TimeFilter,
    showTimeFilters: Boolean,
    onToggleTimeFilters: () -> Unit,
    onFilterSelected: (TimeFilter) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMetrics: () -> Unit,
    onSelectDateClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimensions.None
    ) {
        Column {
            TopAppBar(
                title = {
                    Column {
                        Text(AppStrings.TimelineTitle, style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(Dimensions.PaddingSmall))
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onSelectDateClick() }
                                .padding(horizontal = Dimensions.PaddingSmall, vertical = Dimensions.Half),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(AppStrings.TimelineToday, style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
                            Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                            val dateText = remember(selectedDate) {
                                val date = selectedDate ?: Clock.System.now()
                                TimeFormatter.formatDate(date)
                            }
                            Text(dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMetrics) {
                        Icon(Icons.Default.BarChart, "Metrics")
                    }
                    IconButton(onClick = onToggleTimeFilters) {
                        Icon(Icons.Default.DateRange, AppStrings.TimelineTimeOfDay)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, AppStrings.TimelineSettings)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )

            AnimatedVisibility(
                visible = showTimeFilters,
                enter = fadeIn(animationSpec = tween(Dimensions.FilterRevealDurationMs)) +
                        expandVertically(
                            animationSpec = tween(Dimensions.FilterRevealDurationMs),
                            expandFrom = Alignment.Top
                        ),
                exit = fadeOut(animationSpec = tween(Dimensions.FilterHideDurationMs)) +
                        shrinkVertically(
                            animationSpec = tween(Dimensions.FilterHideDurationMs),
                            shrinkTowards = Alignment.Top
                        )
            ) {
                TimelineFilterSection(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }
        }
    }
}

@Composable
fun TimelineFilterSection(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = Dimensions.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
        contentPadding = PaddingValues(end = Dimensions.PaddingMedium)
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
        // Time
        val timeText = remember(session.startTime) {
            TimeFormatter.formatTime(session.startTime)
        }
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(Dimensions.PaddingLarge * 2) // 48dp
        )

        // Line and Dot
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

        // Card
        Card(
            modifier = Modifier
                .weight(AppWeights.Full)
                .padding(vertical = Dimensions.PaddingSmall)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Divider)
            ),
            shape = MaterialTheme.shapes.medium
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
                    text = "${session.durationMinutes}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
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
    summary: TimelineSummary,
    onUpgradeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(Dimensions.PaddingMedium)
            .navigationBarsPadding()
            .fillMaxWidth(),
        shape =  MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = Dimensions.ModalElevation,
        shadowElevation = Dimensions.ModalElevation
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .clip(MaterialTheme.shapes.medium)
                .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onUpgradeClick() }
                    .padding(Dimensions.Half)
            ) {
                Text(AppStrings.TimelineTotalUsage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("${summary.totalHours}h ${summary.totalMinutes}m", style = MaterialTheme.typography.titleMedium)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onUpgradeClick() }
                    .padding(Dimensions.Half)
            ) {
                Text(AppStrings.TimelineSessionsCount, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(summary.sessionCount.toString(), style = MaterialTheme.typography.titleMedium)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onUpgradeClick() }
                    .padding(Dimensions.Half)
            ) {
                Text(AppStrings.TimelineMostUsed, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
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
