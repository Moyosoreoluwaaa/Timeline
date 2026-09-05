package com.timeline.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
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
import com.timeline.ui.LocalNavAnimatedVisibilityScope
import com.timeline.ui.LocalSharedTransitionScope

@Composable
fun SessionDetailSheet(
    state: TimelineState,
    expansionProgress: () -> Float,
    prevSession: Session?,
    nextSession: Session?,
    onEvent: (TimelineEvent) -> Unit,
    onShowFullScreenImage: (String) -> Unit
) {
    val session = state.selectedSession ?: return

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (state.isSheetExpanded) Modifier.fillMaxHeight() else Modifier)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        SessionDetailHeader(
            session = session,
            isExpanded = state.isSheetExpanded,
            totalSessions = state.relatedSessions.size
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
                                val progress = expansionProgress()
                                alpha = AppAlpha.Full - progress
                                translationY = -progress * Dimensions.SpacingHuge.value
                            }
                    ) {
                        StatRow(session)
                        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)
                        ) {
                            val segmentsWithScreenshots =
                                session.segments.filter { it.screenshotPath != null }
                            items(segmentsWithScreenshots) { segment ->
                                val path = segment.screenshotPath!!
                                val modifier =
                                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                        with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            rememberSharedContentState(key = "image-$path"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = { _, _ -> spring(dampingRatio = 0.8f, stiffness = 380f) }
                                        )
                                    }
                                    } else Modifier

                                ScreenshotImage(
                                    path = path,
                                    contentDescription = AppStrings.ContentDescSessionSegment,
                                    modifier = Modifier
                                        .width(Dimensions.SpacingMega)
                                        .height(Dimensions.SpacingUltra)
                                        .clip(MaterialTheme.shapes.small)
                                        .then(modifier)
                                        .clickable { onShowFullScreenImage(path) }
                                )
                            }
                        }
                    }

                    ExpandedSessionContent(
                        appSessions = state.relatedSessions,
                        progress = expansionProgress,
                        onImageClick = { path ->
                            if (path != null) onShowFullScreenImage(path)
                        }
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
