package com.timeline.domain.reasoning

import com.timeline.domain.AppInfoProvider
import com.timeline.domain.AppMetadata
import com.timeline.domain.Session
import com.timeline.presentation.TimeOfDayFilter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HighlightContentSynthesizerTest {

    private val fakeAppInfoProvider = object : AppInfoProvider {
        override suspend fun getAppName(packageName: String): String = when (packageName) {
            "com.android.chrome" -> "Chrome"
            "com.google.android.youtube" -> "YouTube"
            "com.google.android.studio" -> "Android Studio"
            "com.Slack" -> "Slack"
            "com.google.android.keep" -> "Keep Notes"
            else -> packageName
        }
        override suspend fun getAppIcon(packageName: String): Any? = null
        override suspend fun getInstalledApps(): List<AppMetadata> = emptyList()
    }

    private val tz = TimeZone.UTC

    @Test
    fun testSynthesizeDayRecapProducesRichFactualHighlights() = kotlinx.coroutines.test.runTest {
        // Morning session: 08:00 UTC (45m in Chrome)
        val s1 = Session(
            id = "1",
            packageName = "com.android.chrome",
            displayName = "Chrome",
            startTime = Instant.parse("2026-10-28T08:00:00Z"),
            endTime = Instant.parse("2026-10-28T08:45:00Z"),
            durationMinutes = 45
        )
        // Afternoon session: 13:00 UTC (120m in Android Studio)
        val s2 = Session(
            id = "2",
            packageName = "com.google.android.studio",
            displayName = "Android Studio",
            startTime = Instant.parse("2026-10-28T13:00:00Z"),
            endTime = Instant.parse("2026-10-28T15:00:00Z"),
            durationMinutes = 120
        )
        // Afternoon session: 15:30 UTC (30m in Slack)
        val s3 = Session(
            id = "3",
            packageName = "com.Slack",
            displayName = "Slack",
            startTime = Instant.parse("2026-10-28T15:30:00Z"),
            endTime = Instant.parse("2026-10-28T16:00:00Z"),
            durationMinutes = 30
        )
        // Evening session: 19:00 UTC (40m in YouTube)
        val s4 = Session(
            id = "4",
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            startTime = Instant.parse("2026-10-28T19:00:00Z"),
            endTime = Instant.parse("2026-10-28T19:40:00Z"),
            durationMinutes = 40
        )

        val sessions = listOf(s1, s2, s3, s4)
        val result = HighlightContentSynthesizer.synthesizeDayRecap(sessions, fakeAppInfoProvider, tz)

        assertEquals(235, result.totalMinutes) // 45 + 120 + 30 + 40 = 235 (3h 55m)
        assertEquals(4, result.sessionCount)
        assertEquals(4, result.topApps.size)
        assertEquals("Android Studio", result.topApps.first().displayName)
        assertEquals(120, result.topApps.first().totalMinutes)

        // Peak window should be afternoon (150m)
        assertNotNull(result.peakWindow)
        assertTrue(result.peakWindow!!.contains("Afternoon"))

        // Cognitive mode should reflect dominant usage (Android Studio = Deep Focus)
        assertEquals("Deep Focus", result.cognitiveMode)

        // Narrative contains app names and total time
        assertTrue(result.narrative.contains("Android Studio"))
        assertTrue(result.narrative.contains("3h 55m"))

        // Smart action items generated for development & communication
        assertTrue(result.actionItems.isNotEmpty())
    }

    @Test
    fun testSynthesizePeriodHighlightDifferentiatesMorningAndEvening() = kotlinx.coroutines.test.runTest {
        val morningSession = Session(
            id = "m1",
            packageName = "com.android.chrome",
            displayName = "Chrome",
            startTime = Instant.parse("2026-10-28T09:00:00Z"),
            endTime = Instant.parse("2026-10-28T09:30:00Z"),
            durationMinutes = 30
        )

        val eveningSession = Session(
            id = "e1",
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            startTime = Instant.parse("2026-10-28T20:00:00Z"),
            endTime = Instant.parse("2026-10-28T20:45:00Z"),
            durationMinutes = 45
        )

        val morningResult = HighlightContentSynthesizer.synthesizePeriodHighlight(
            TimeOfDayFilter.MORNING, listOf(morningSession), fakeAppInfoProvider, tz
        )
        val eveningResult = HighlightContentSynthesizer.synthesizePeriodHighlight(
            TimeOfDayFilter.EVENING, listOf(eveningSession), fakeAppInfoProvider, tz
        )

        assertTrue(morningResult.narrative.contains("morning"))
        assertTrue(morningResult.narrative.contains("Chrome"))

        assertTrue(eveningResult.narrative.contains("evening"))
        assertTrue(eveningResult.narrative.contains("YouTube"))
        assertEquals("Casual", eveningResult.cognitiveMode)
    }

    @Test
    fun testExtractSmartActionItemsFromOcr() {
        val ocr = listOf(
            "Here is the plan:\n• Review quarterly analytics report\n• Submit bug fix for ticket #402\nSome random noise"
        )
        val items = HighlightContentSynthesizer.extractSmartActionItems(emptyList(), ocr)
        assertEquals(2, items.size)
        assertTrue(items.any { it.contains("Review quarterly analytics report") })
        assertTrue(items.any { it.contains("Submit bug fix for ticket #402") })
    }
}
