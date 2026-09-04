package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineState
import com.timeline.ui.components.*
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun SessionDetailSheet(
    state: TimelineState,
    expansionProgress: Float,
    prevSession: Session?,
    nextSession: Session?,
    onEvent: (TimelineEvent) -> Unit
) {
    val session = state.selectedSession ?: return
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (state.isSheetExpanded) Modifier.fillMaxHeight() else Modifier)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SessionDetailHeader(
                session = session,
                isExpanded = state.isSheetExpanded,
                totalAppSessions = state.relatedSessions.size
            )

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimensions.PaddingMedium)
                                .padding(top = Dimensions.PaddingMedium)
                                .padding(bottom = Dimensions.IconHuge)
                                .graphicsLayer {
                                    alpha = AppAlpha.Full - expansionProgress
                                    translationY = -expansionProgress * Dimensions.SpacingHuge.value
                                }
                        ) {
                            StatRow(session)
                            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)
                            ) {
                                val segmentsWithScreenshots = session.segments.filter { it.screenshotPath != null }
                                items(segmentsWithScreenshots) { segment ->
                                    ScreenshotImage(
                                        path = segment.screenshotPath,
                                        contentDescription = AppStrings.ContentDescSessionSegment,
                                        modifier = Modifier
                                            .width(Dimensions.SpacingMega)
                                            .height(Dimensions.SpacingUltra)
                                            .clickable { onEvent(TimelineEvent.ShowFullScreenImage(segment.screenshotPath)) }
                                    )
                                }
                            }
                        }

                        ExpandedSessionContent(
                            appSessions = state.relatedSessions,
                            progress = expansionProgress,
                            onImageClick = { onEvent(TimelineEvent.ShowFullScreenImage(it)) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        SessionDetailFooter(
                            prevSession = prevSession,
                            nextSession = nextSession,
                            onPrevClick = { onEvent(TimelineEvent.SelectPreviousSession) },
                            onNextClick = { onEvent(TimelineEvent.SelectNextSession) }
                        )
                    }
                }
            }
        }
    }
}