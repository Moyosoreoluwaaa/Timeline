package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent
import com.timeline.ui.components.*

@Composable
fun SessionDetailSheet(
    session: Session,
    allSessions: List<Session>,
    isExpanded: Boolean,
    expansionProgress: Float,
    prevSession: Session?,
    nextSession: Session?,
    onEvent: (TimelineEvent) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().then(if (isExpanded) Modifier.fillMaxHeight() else Modifier)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SessionDetailHeader(
                session = session,
                isExpanded = isExpanded,
                onToggleExpanded = { onEvent(TimelineEvent.ToggleSheet(!isExpanded)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 80.dp)
                        .graphicsLayer {
                            alpha = 1f - expansionProgress
                            translationY = -expansionProgress * 50.dp.toPx()
                        }
                ) {
                    StatRow(session)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val segmentsWithScreenshots = session.segments.filter { it.screenshotPath != null }
                        items(segmentsWithScreenshots) { segment ->
                            ScreenshotImage(
                                path = segment.screenshotPath,
                                contentDescription = "Session segment",
                                modifier = Modifier.width(120.dp).height(180.dp)
                            )
                        }
                    }
                }

                ExpandedSessionContent(currentSession = session, allSessions = allSessions, progress = expansionProgress)
            }
        }

        SessionDetailFooter(
            prevSession = prevSession,
            nextSession = nextSession,
            onPrevClick = { onEvent(TimelineEvent.SelectPreviousSession) },
            onNextClick = { onEvent(TimelineEvent.SelectNextSession) }
        )
    }
}
