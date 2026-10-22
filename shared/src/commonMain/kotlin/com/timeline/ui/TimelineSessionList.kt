package com.timeline.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineSummary
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.BottomSummary
import com.timeline.ui.components.TimelineEntry
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions

@Composable
internal fun TimelineSessionList(
    sessions: List<Session>,
    summary: TimelineSummary?,
    isShowingShimmer: Boolean,
    navBarPadding: Dp,
    topPadding: Dp,
    onSessionClick: (Session) -> Unit,
    onSummaryClick: () -> Unit,
    tutorialViewModel: TutorialViewModel,
    onBoundsCalculated: (TutorialStep, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(AppWeights.Full)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimelineEmptyContent(
                        isShowingShimmer = isShowingShimmer,
                        navBarPadding = navBarPadding
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(AppWeights.Full)
                        .fillMaxWidth()
                        .padding(top = topPadding),
                    contentPadding = PaddingValues(
                        top = Dimensions.PaddingSmall,
                        bottom = navBarPadding + (Dimensions.PaddingLarge * 2)
                    )
                ) {
                    itemsIndexed(
                        items = sessions,
                        key = { _, session -> session.id }
                    ) { index, session ->
                        val isMiddleItem = index == sessions.size / 2

                        TimelineEntry(
                            session = session,
                            isFirst = index == 0,
                            isLast = index == sessions.lastIndex,
                            modifier = Modifier
                                .animateItem()
                                .then(
                                    if (isMiddleItem) {
                                        Modifier.spotlightTarget(
                                            TutorialStep.SPOTLIGHT_APP_ENTRY,
                                            onBoundsCalculated
                                        )
                                    } else Modifier
                                )
                        ) { onSessionClick(session) }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                    if (coordinates.isAttached) {
                        onBoundsCalculated(
                            TutorialStep.SPOTLIGHT_SUMMARY_BAR,
                            coordinates.boundsInWindow()
                        )
                    }
                }
        ) {
            BottomSummary(
                summary = summary,
                onSummaryClick = onSummaryClick,
                modifier = Modifier.spotlightTarget(
                    TutorialStep.SPOTLIGHT_SUMMARY_BAR,
                    onBoundsCalculated
                ),
                tutorialViewModel = tutorialViewModel
            )
        }
    }
}
