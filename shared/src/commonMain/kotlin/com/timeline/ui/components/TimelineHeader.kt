package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timeline.presentation.TimeFilter
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.theme.Dimensions
import com.timeline.util.TimeFormatter
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
    onSelectDateClick: () -> Unit,
    isSettingsActive: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> }
) {
    val yellowAccent = Color(0xFFFFD54F)

    val topBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    CustomTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                Column {
                    Text(
                        text = "Timeline",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        color = Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .clickable(onClick = onSelectDateClick)
                            .spotlightTarget(
                                TutorialStep.SPOTLIGHT_DATE_CONTAINER,
                                onBoundsCalculated
                            )
                    ) {
                        Text(
                            text = selectedDate?.let { TimeFormatter.formatDate(it) } ?: "Today, September 14, 2026",
                            style = MaterialTheme.typography.labelMedium.copy(color = yellowAccent),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onToggleTimeFilters,
                modifier = Modifier.spotlightTarget(
                    TutorialStep.SPOTLIGHT_TIME_FILTER_ICON,
                    onBoundsCalculated
                )
            ) {
                Icon(
                    imageVector = if (showTimeFilters) Icons.Filled.Schedule else Icons.Outlined.Schedule,
                    contentDescription = "Toggle Time Filters"
                )
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.spotlightTarget(
                    TutorialStep.SPOTLIGHT_SETTINGS_ICON,
                    onBoundsCalculated
                )
            ) {
                Icon(
                    imageVector = if (isSettingsActive) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        colors = topBarColors,
        scrollBehavior = scrollBehavior,
        cutoutRadius = TopAppBarCutoutRadius,
        expandableContent = {
            AnimatedVisibility(
                visible = showTimeFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TimelineFilterSection(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    backgroundColor = topBarColors.containerColor,
                    modifier = Modifier.spotlightTarget(
                        TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION,
                        onBoundsCalculated
                    )
                )
            }
        }
    )
}

@Composable
fun TimelineFilterSection(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}