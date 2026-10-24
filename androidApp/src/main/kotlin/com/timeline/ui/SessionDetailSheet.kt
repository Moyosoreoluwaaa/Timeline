package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineState
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.ExpandedSessionContent
import com.timeline.ui.components.SessionDetailFooter
import com.timeline.ui.components.SessionDetailHeader
import com.timeline.ui.components.StatRow
import com.timeline.ui.components.clickableWithBounds
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings


@Composable
fun SessionDetailSheet(
    state: TimelineState,
    expansionProgress: () -> Float,
    prevSession: Session?,
    nextSession: Session?,
    onEvent: (TimelineEvent) -> Unit,
    onShowFullScreenImage: (String, Rect) -> Unit,
    onBoundsCalculated: ((TutorialStep, Rect) -> Unit)? = null,
    // True only when the collapsed thumbnail row is actually the visible,
    // opaque target -- i.e. sheet is collapsed, not mid-expansion.
    isThumbnailRowVisible: () -> Boolean = { true },
) {
    val session = state.selectedSession ?: return

    LaunchedEffect(session.id, state.relatedSessions) {
        prefetchScreenshots(
            state.relatedSessions.ifEmpty { listOf(session) }
                .flatMap { it.segments }
                .mapNotNull { it.screenshotPath }
                .distinct()
                .take(60)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        SessionDetailHeader(
            session = session,
            expansionProgress = expansionProgress,
            relatedSessions = state.relatedSessions.ifEmpty { listOf(session) }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Compose the expanded layer only once the drag has begun, so it
                // never sits invisibly on top of the collapsed thumbnails and
                // swallows their taps.
                val expandedVisible by remember { derivedStateOf { expansionProgress() > 0.01f } }
                val collapsedInteractive by remember { derivedStateOf { expansionProgress() < 0.5f } }

                // ── Collapsed view: stat row + scrollable images ───────────
                val allSegments = remember(state.relatedSessions, session) {
                    val targetSessions = state.relatedSessions.ifEmpty { listOf(session) }
                    targetSessions
                        .flatMap { it.segments }
                        .filter { it.screenshotPath != null }
                        .distinctBy { it.screenshotPath }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.PaddingMedium)
                        .padding(top = Dimensions.PaddingMedium)
                        .graphicsLayer {
                            val p = expansionProgress()
                            // Fades out over the first 70% of the drag, drifting down
                            // toward where the first expanded card appears
                            alpha = (1f - p / 0.7f).coerceIn(0f, 1f)
                            translationY = p * Dimensions.PaddingSmall.toPx()
                        }
                ) {
                    StatRow(session)
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                    if (allSegments.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
                            contentPadding = PaddingValues(end = Dimensions.PaddingMedium)
                        ) {
                            itemsIndexed(allSegments) { index, segment ->
                                val path = segment.screenshotPath!!

                                Box(
                                    modifier = Modifier
                                        .width(Dimensions.SpacingMega)
                                        .height(Dimensions.SpacingUltra)
                                        .clip(MaterialTheme.shapes.small)
                                        .clickableWithBounds(enabled = collapsedInteractive) {
                                            onShowFullScreenImage(
                                                path,
                                                it
                                            )
                                        }
                                        .then(
                                            if (index == 0 && onBoundsCalculated != null) {
                                                Modifier.spotlightTarget(
                                                    step = TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL,
                                                    onBoundsCalculated = onBoundsCalculated,
                                                    isEligible = isThumbnailRowVisible
                                                )
                                            } else Modifier
                                        )
                                ) {
                                    ScreenshotImage(
                                        path = path,
                                        contentDescription = AppStrings.ContentDescSessionSegment,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(1.35f), // Keyboard crop
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Expanded view (composed only when visible) ─────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, if (expandedVisible) 0 else 100_000) }
                ) {
                    ExpandedSessionContent(
                        appSessions = state.relatedSessions.ifEmpty { listOf(session) },
                        progress = expansionProgress,
                        interactive = expandedVisible,
                        onImageClick = onShowFullScreenImage
                    )
                }

                // ── Bottom footer: prev / next navigation ──────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    SessionDetailFooter(
                        prevSession = prevSession,
                        nextSession = nextSession,
                        onPrevClick = { onEvent(TimelineEvent.SelectPreviousSession) },
                        onNextClick = { onEvent(TimelineEvent.SelectNextSession) },
                        onBoundsCalculated = { rect ->
                            onBoundsCalculated?.invoke(
                                TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR,
                                rect
                            )
                        }
                    )
                }
            }
        }
    }
}