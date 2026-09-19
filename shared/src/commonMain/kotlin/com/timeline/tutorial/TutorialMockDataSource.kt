package com.timeline.tutorial

import com.timeline.domain.Session
import com.timeline.domain.SessionSegment
import kotlin.time.Clock
import kotlin.time.Instant

interface TutorialMockDataSource {
    suspend fun getInitialTutorialSessions(): List<Session>
}

class TutorialMockDataSourceImpl : TutorialMockDataSource {
    override suspend fun getInitialTutorialSessions(): List<Session> {
        val nowMillis = Clock.System.now().toEpochMilliseconds()

        // 1. Previous Session: YouTube
        val session1 = Session(
            id = "tutorial_session_youtube",
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            startTime = Instant.fromEpochMilliseconds(nowMillis - 3_600_000),
            endTime = Instant.fromEpochMilliseconds(nowMillis - 2_700_000),
            durationMinutes = 15,
            durationSeconds = 0,
            screenshots = listOf("mock_youtube_1.jpg"),
            segments = listOf(
                SessionSegment(
                    timestamp = Instant.fromEpochMilliseconds(nowMillis - 3_600_000),
                    screenshotPath = "mock_youtube_1.jpg",
                    activityDescription = "Browsing Subscriptions"
                )
            )
        )

        // 2. Middle Spotlighted Session: Timeline Records
        val session2 = Session(
            id = "tutorial_session_timeline_records_middle",
            packageName = "com.timeline_records",
            displayName = "Timeline Records",
            startTime = Instant.fromEpochMilliseconds(nowMillis - 2_400_000),
            endTime = Instant.fromEpochMilliseconds(nowMillis - 900_000),
            durationMinutes = 25,
            durationSeconds = 0,
            screenshots = listOf("mock_timeline_records1.jpg", "mock_timeline_records2.jpg"),
            segments = listOf(
                SessionSegment(
                    timestamp = Instant.fromEpochMilliseconds(nowMillis - 2_400_000),
                    screenshotPath = "mock_timeline_records1.jpg",
                    activityDescription = "Watching Android Dev Tutorial"
                ),
                SessionSegment(
                    timestamp = Instant.fromEpochMilliseconds(nowMillis - 1_500_000),
                    screenshotPath = "mock_timeline_records2.jpg",
                    activityDescription = "Using Timeline"
                )
            )
        )

        // 3. Next Session: X
        val session3 = Session(
            id = "tutorial_session_x",
            packageName = "com.twitter.android",
            displayName = "X",
            startTime = Instant.fromEpochMilliseconds(nowMillis - 600_000),
            endTime = Instant.fromEpochMilliseconds(nowMillis),
            durationMinutes = 10,
            durationSeconds = 0,
            screenshots = listOf("mock_x_1.jpg"),
            segments = listOf(
                SessionSegment(
                    timestamp = Instant.fromEpochMilliseconds(nowMillis - 600_000),
                    screenshotPath = "mock_x_1.jpg",
                    activityDescription = "Scrolling timeline"
                )
            )
        )

        return listOf(session1, session2, session3)
    }
}