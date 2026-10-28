package com.timeline.presentation

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.timeline.data.AnalysisResultEntity
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.AppMetadata
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.Session
import com.timeline.domain.reasoning.AppDailyReasoning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SimplifiedHighlightViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeAppInfoProvider = object : AppInfoProvider {
        override suspend fun getAppName(packageName: String): String = "TestApp"
        override suspend fun getAppIcon(packageName: String): Any? = null
        override suspend fun getInstalledApps(): List<AppMetadata> = emptyList()
    }

    private val fakeSyncer = object : DeviceUsageSyncer {
        override fun hasPermission(): Boolean = true
        override suspend fun syncRealDeviceUsage(): Int = 0
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateAndSessionProcessing() = runTest(testDispatcher) {
        val now = kotlin.time.Clock.System.now()
        val s1 = Session(
            id = "1",
            packageName = "com.test.app",
            displayName = "TestApp",
            startTime = now,
            endTime = now,
            durationMinutes = 20
        )

        val fakeRepo = object : TimelineRepository {
            override fun getTimeline(): Flow<List<Session>> = flowOf(listOf(s1))
            override suspend fun saveSession(session: Session) {}
            override fun getAppDailyReasoning(packageName: String, date: String): Flow<AppDailyReasoning?> = flowOf(null)
            override suspend fun saveAppDailyReasoning(reasoning: AppDailyReasoning) {}
            override suspend fun getAnalysisResult(screenshotPath: String): AnalysisResultEntity? = null
            override suspend fun saveAnalysisResult(screenshotPath: String, result: com.timeline.domain.ml.ImageAnalysisResult) {}
        }

        val viewModel = SimplifiedHighlightViewModel(
            repository = fakeRepo,
            appInfoProvider = fakeAppInfoProvider,
            deviceUsageSyncer = fakeSyncer,
            logger = Logger(config = StaticConfig())
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(3, state.segments.size)
        assertTrue(state.isUsagePermissionGranted)
    }
}
