package com.timeline.ui

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
    tutorialStep: TutorialStep? = null,
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val defaultOrder = remember {
        listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.AFTERNOON, TimeOfDayFilter.EVENING)
    }

    val sortedSegments = remember(segments, activeFilter) {
        val active = if (activeFilter == TimeOfDayFilter.ALL) TimeOfDayFilter.EVENING else activeFilter
        val back = defaultOrder.filter { it != active }
        val orderedFilters = back + active
        orderedFilters.mapNotNull { filter -> segments.find { it.filter == filter } }
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
