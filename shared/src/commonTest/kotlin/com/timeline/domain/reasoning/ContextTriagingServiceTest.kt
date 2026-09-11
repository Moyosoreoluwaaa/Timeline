package com.timeline.domain.reasoning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContextTriagingServiceTest {

    @Test
    fun testLowConfidenceFramesFilteredOut() {
        val frames = listOf(
            TriagedFrame(
                sessionId = "s1",
                packageName = "com.slack",
                timestamp = 1000L,
                text = "Blurry unreadable text",
                labels = listOf("Screen"),
                confidenceScore = 0.20f,
                visualCategory = null
            ),
            TriagedFrame(
                sessionId = "s1",
                packageName = "com.slack",
                timestamp = 2000L,
                text = "Standup meeting updates discussed with the mobile team",
                labels = listOf("Text"),
                confidenceScore = 0.85f,
                visualCategory = "Communication"
            )
        )

        val result = ContextTriagingService.triageSession(
            packageName = "com.slack",
            appName = "Slack",
            date = "2026-09-11",
            frames = frames
        )

        assertEquals(1, result.frames.size)
        assertTrue(result.deduplicatedTextSamples.first().contains("Standup meeting updates"))
    }

    @Test
    fun testIntraSessionConsecutiveDeduplication() {
        val frame1 = TriagedFrame(
            sessionId = "session_1",
            packageName = "com.slack",
            timestamp = 1000L,
            text = "Discussing Q3 sprint deliverables roadmap and milestone dates with team",
            labels = listOf("Text"),
            confidenceScore = 0.9f,
            visualCategory = "Communication"
        )
        // Highly similar frame (>85% overlap) within the same session
        val frame2 = TriagedFrame(
            sessionId = "session_1",
            packageName = "com.slack",
            timestamp = 2000L,
            text = "Discussing Q3 sprint deliverables roadmap and milestone dates with team members",
            labels = listOf("Text"),
            confidenceScore = 0.95f,
            visualCategory = "Communication"
        )

        val result = ContextTriagingService.triageSession(
            packageName = "com.slack",
            appName = "Slack",
            date = "2026-09-11",
            frames = listOf(frame1, frame2)
        )

        assertEquals(1, result.frames.size)
        assertTrue(result.frames.first().text.contains("team members"))
    }

    @Test
    fun testCrossSessionFramesNotMerged() {
        val text = "Discussing project roadmap and delivery timelines"
        val frame1 = TriagedFrame(
            sessionId = "session_1",
            packageName = "com.slack",
            timestamp = 1000L,
            text = text,
            labels = listOf("Text"),
            confidenceScore = 0.9f,
            visualCategory = "Communication"
        )
        // Same text, but in a different session (app was reopened later)
        val frame2 = TriagedFrame(
            sessionId = "session_2",
            packageName = "com.slack",
            timestamp = 5000L,
            text = text,
            labels = listOf("Text"),
            confidenceScore = 0.9f,
            visualCategory = "Communication"
        )

        val result = ContextTriagingService.triageSession(
            packageName = "com.slack",
            appName = "Slack",
            date = "2026-09-11",
            frames = listOf(frame1, frame2)
        )

        // Across sessions, frames are maintained
        assertEquals(2, result.frames.size)
    }

    @Test
    fun testYesterdaySummaryOmittedWhenBlank() {
        val result = ContextTriagingService.triageSession(
            packageName = "com.slack",
            appName = "Slack",
            date = "2026-09-11",
            frames = emptyList(),
            yesterdaySummary = "   "
        )
        assertNull(result.previousDaySummary)
    }

    @Test
    fun testBoilerplateStripping() {
        val raw = """
            9:41 AM
            Back
            Settings
            Sprint review notes: Completed auth feature and room database migrations.
            Share
            More options
        """.trimIndent()

        val cleaned = ContextTriagingService.cleanBoilerplate(raw)
        assertTrue(!cleaned.contains("9:41 AM"))
        assertTrue(!cleaned.contains("Back"))
        assertTrue(!cleaned.contains("Settings"))
        assertTrue(cleaned.contains("Sprint review notes: Completed auth feature"))
    }
}
