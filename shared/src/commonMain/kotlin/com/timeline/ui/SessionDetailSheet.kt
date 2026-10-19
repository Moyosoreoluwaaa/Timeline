package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import com.timeline.presentation.TimelineState
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.*
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun SessionDetailSheet(
    state: TimelineState,
    expansionProgress: () -> Float,
    prevSession: Session?,
    nextSession: Session?,
    onEvent: (TimelineEvent) -> Unit,
    onShowFullScreenImage: (String) -> Unit,
    onBoundsCalculated: ((TutorialStep, Rect) -> Unit)? = null,
    // True only when the collapsed thumbnail row is actually the visible,
    // opaque target -- i.e. sheet is collapsed, not mid-expansion. Prevents
    // the tutorial from spotlighting stale bounds captured right as this
    // row fades out (alpha = Full - expansionProgress, below).
    isThumbnailRowVisible: () -> Boolean = { true },
    // Overrides state.isSheetExpanded for layout purposes (header's "isExpanded"
    // label). Defaults to the raw view-model value; TimelineScreen passes the
    // tutorial's effective value while a tutorial step is controlling the sheet.
    isSheetExpandedOverride: Boolean? = null
) {
    val session = state.selectedSession ?: return
    val isSheetExpanded = isSheetExpandedOverride ?: state.isSheetExpanded

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        SessionDetailHeader(
            session = session,
            isExpanded = isSheetExpanded,
            relatedSessions = state.relatedSessions.ifEmpty { listOf(session) }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Collapsed view: stat row + scrollable images ───────────
                // Fades out and slides up as the sheet expands
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
                            val progress = expansionProgress()
                            alpha = AppAlpha.Full - progress
                            translationY = -progress * Dimensions.SpacingHuge.value
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
                                        .clickable { onShowFullScreenImage(path) }
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

                                    // Overlay label: capture time only
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth(),
                                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = segment.timestamp.formatTime(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = com.timeline.ui.theme.AppColors.BrandYellow
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Expanded view: full session detail fills screen height ─
                // Fades in and slides up from below as the sheet expands
                ExpandedSessionContent(
                    appSessions = state.relatedSessions.ifEmpty { listOf(session) },
                    progress = expansionProgress,
                    onImageClick = { path ->
                        if (path != null) onShowFullScreenImage(path)
                    }
                )

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
                            onBoundsCalculated?.invoke(TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR, rect)
                        }
                    )
                }
            }
        }
    }
}