package com.timeline.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.timeline.presentation.HighlightSegment
import com.timeline.presentation.TimeOfDayFilter
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget

@Composable
internal fun HighlightCardStack(
    segments: List<HighlightSegment>,
    activeFilter: TimeOfDayFilter,
    onSelectFilter: (TimeOfDayFilter) -> Unit,
    digestFrequency: Int = 3,
    tutorialStep: TutorialStep? = null,
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> },
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val defaultOrder = remember {
        listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.AFTERNOON, TimeOfDayFilter.EVENING)
    }

    // Cap visible cards based on digestFrequency: 1=Active/Evening, 2=Morning+Evening, 3=All
    val allowedFilters = remember(digestFrequency, segments) {
        val base = when (digestFrequency.coerceIn(1, 3)) {
            1 -> {
                val hasEvening = segments.any { it.filter == TimeOfDayFilter.EVENING && it.apps.isNotEmpty() }
                val hasAfternoon = segments.any { it.filter == TimeOfDayFilter.AFTERNOON && it.apps.isNotEmpty() }
                val hasMorning = segments.any { it.filter == TimeOfDayFilter.MORNING && it.apps.isNotEmpty() }
                when {
                    hasEvening -> listOf(TimeOfDayFilter.EVENING)
                    hasAfternoon -> listOf(TimeOfDayFilter.AFTERNOON)
                    hasMorning -> listOf(TimeOfDayFilter.MORNING)
                    else -> listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.AFTERNOON, TimeOfDayFilter.EVENING)
                }
            }
            2 -> listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.EVENING)
            else -> listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.AFTERNOON, TimeOfDayFilter.EVENING)
        }
        if (segments.any { it.apps.isNotEmpty() } && segments.filter { it.filter in base && it.apps.isNotEmpty() }.isEmpty()) {
            listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.AFTERNOON, TimeOfDayFilter.EVENING)
        } else {
            base
        }
    }

    val filteredSegments = remember(segments, allowedFilters) {
        segments.filter { it.filter in allowedFilters }
    }

    val sortedSegments = remember(filteredSegments, activeFilter) {
        val active = if (activeFilter == TimeOfDayFilter.ALL) TimeOfDayFilter.EVENING else activeFilter
        val back = defaultOrder.filter { it != active && it in allowedFilters }
        val orderedFilters = back + active
        orderedFilters.mapNotNull { filter -> filteredSegments.find { it.filter == filter } }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        sortedSegments.forEachIndexed { stackIndex, segment ->
            val isFrontmost = stackIndex == sortedSegments.lastIndex
            val offsetFromTop by animateDpAsState(
                targetValue = when (stackIndex) {
                    0 -> 0.dp
                    1 -> 56.dp
                    else -> 112.dp
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "CardTopOffset"
            )

            val elevation by animateDpAsState(
                targetValue = ((stackIndex + 1) * 4).dp,
                label = "CardElevation"
            )

            HighlightSegmentCard(
                segment = segment,
                isFrontmost = isFrontmost,
                offsetFromTop = offsetFromTop,
                elevation = elevation,
                onHeaderClick = {
                    onSelectFilter(segment.filter)
                },
                modifier = if (isFrontmost) Modifier.spotlightTarget(
                    TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD,
                    onBoundsCalculated
                ) else Modifier
            )
        }
    }
}
