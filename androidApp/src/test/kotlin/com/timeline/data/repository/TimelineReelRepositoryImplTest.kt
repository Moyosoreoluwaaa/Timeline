package com.timeline.data.repository

import com.timeline.data.AnalysisResultEntity
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.AppMetadata
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.Session
import com.timeline.domain.model.ReelSlideKind
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.presentation.TimeOfDayFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TimelineReelRepositoryImplTest {

    private val fakeAppInfoProvider = object : AppInfoProvider {
        override suspend fun getAppName(packageName: String): String = when (packageName) {
            "com.android.chrome" -> "Chrome"
            "com.google.android.youtube" -> "YouTube"
            else -> packageName
        }
        override suspend fun getAppIcon(packageName: String): Any? = null
        override suspend fun getInstalledApps(): List<AppMetadata> = emptyList()
    }

    private val fakeSyncer = object : DeviceUsageSyncer {
        override fun hasPermission(): Boolean = true
        override suspend fun syncRealDeviceUsage(): Int = 0
    }

    @Test
    fun testGetDaySegmentsEmitsDistinctPeriodNarratives() = kotlinx.coroutines.test.runTest {
        val now = kotlin.time.Clock.System.now()
        val s1 = Session(
            id = "1",
            packageName = "com.android.chrome",
            displayName = "Chrome",
            startTime = now,
            endTime = now,
            durationMinutes = 30
        )

        val fakeRepo = object : TimelineRepository {
            override fun getTimeline(): Flow<List<Session>> = flowOf(listOf(s1))
            override suspend fun saveSession(session: Session) {}
            override fun getAppDailyReasoning(packageName: String, date: String): Flow<AppDailyReasoning?> = flowOf(null)
            override suspend fun saveAppDailyReasoning(reasoning: AppDailyReasoning) {}
            override suspend fun getAnalysisResult(screenshotPath: String): AnalysisResultEntity? = null
            override suspend fun saveAnalysisResult(screenshotPath: String, result: com.timeline.domain.ml.ImageAnalysisResult) {}
        }

        val repository = TimelineReelRepositoryImpl(fakeRepo, fakeAppInfoProvider, fakeSyncer)

        repository.getDaySegments().collect { segments ->
            assertEquals(3, segments.size)
            val morning = segments.find { it.filter == TimeOfDayFilter.MORNING }
            val afternoon = segments.find { it.filter == TimeOfDayFilter.AFTERNOON }
            val evening = segments.find { it.filter == TimeOfDayFilter.EVENING }

            assertNotNull(morning)
            assertNotNull(afternoon)
            assertNotNull(evening)

            // Period narratives should be distinct, not repeating the same generic string
            assertTrue(morning.narrative != afternoon.narrative || morning.narrative != evening.narrative)
        }
    }

    @Test
    fun testGetReelSlidesBuildsMultiSlideStory() = kotlinx.coroutines.test.runTest {
        val now = kotlin.time.Clock.System.now()
        val s1 = Session(
            id = "1",
            packageName = "com.android.chrome",
            displayName = "Chrome",
            startTime = now,
            endTime = now,
            durationMinutes = 35
        )

        val fakeRepo = object : TimelineRepository {
            override fun getTimeline(): Flow<List<Session>> = flowOf(listOf(s1))
            override suspend fun saveSession(session: Session) {}
            override fun getAppDailyReasoning(packageName: String, date: String): Flow<AppDailyReasoning?> = flowOf(null)
            override suspend fun saveAppDailyReasoning(reasoning: AppDailyReasoning) {}
            override suspend fun getAnalysisResult(screenshotPath: String): AnalysisResultEntity? = null
            override suspend fun saveAnalysisResult(screenshotPath: String, result: com.timeline.domain.ml.ImageAnalysisResult) {}
        }

        val repository = TimelineReelRepositoryImpl(fakeRepo, fakeAppInfoProvider, fakeSyncer)

        val slides = repository.getReelSlides(TimeOfDayFilter.ALL)
        assertTrue(slides.isNotEmpty())

        // Slide 1 should be Intro overview
        assertEquals(ReelSlideKind.STAT, slides.first().kind)
        assertTrue(slides.first().headline.isNotEmpty())

        // Since session is 35m (>= 15m), an Outlier Deep Dive slide should be generated
        val outlierSlide = slides.find { it.kind == ReelSlideKind.OUTLIER }
        assertNotNull(outlierSlide)
        assertEquals("Peak Focus", outlierSlide.badgeTag)

        // Final slide should be the stat card
        assertEquals(ReelSlideKind.STAT, slides.last().kind)
        assertTrue(slides.last().keyLabel.contains("Total"))
    }
}
