package com.timeline.presentation

import com.timeline.domain.model.ReelSegment
import com.timeline.domain.model.ReelSlide
import com.timeline.domain.model.ReelSlideKind
import com.timeline.domain.repository.ReelHighlightRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReelHighlightViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : ReelHighlightRepository {
        override fun getDaySegments(): Flow<List<ReelSegment>> = flowOf(
            listOf(
                ReelSegment(
                    filter = TimeOfDayFilter.MORNING,
                    title = "Morning",
                    narrative = "Morning recap",
                    paragraphs = listOf("Morning recap"),
                    accentColor = 0xFF5CD6C4
                ),
                ReelSegment(
                    filter = TimeOfDayFilter.AFTERNOON,
                    title = "Afternoon",
                    narrative = "Afternoon recap",
                    paragraphs = listOf("Afternoon recap"),
                    accentColor = 0xFFFFB938
                ),
                ReelSegment(
                    filter = TimeOfDayFilter.EVENING,
                    title = "Evening",
                    narrative = "Evening recap",
                    paragraphs = listOf("Evening recap"),
                    accentColor = 0xFFA9B0FF
                )
            )
        )

        override suspend fun getReelSlides(filter: TimeOfDayFilter): List<ReelSlide> = listOf(
            ReelSlide(
                id = "slide_1",
                kind = ReelSlideKind.STAT,
                keyLabel = "INTRO",
                headline = "Intro headline",
                bgColorHex = 0xFF5CD6C4,
                fgColorHex = 0xFF06302B,
                accentColorHex = 0xFFB8F0E8
            ),
            ReelSlide(
                id = "slide_2",
                kind = ReelSlideKind.STAT,
                keyLabel = "TOTAL",
                headline = "Total headline",
                bgColorHex = 0xFFF27DC3,
                fgColorHex = 0xFF3A0E2E,
                accentColorHex = 0xFFFFC2E6
            )
        )

        override suspend fun syncRealDeviceUsage() {}
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
    fun testOpenReelAndSlideNavigation() = runTest(testDispatcher) {
        val viewModel = ReelHighlightViewModel(fakeRepo)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.segments.size)
        assertNull(viewModel.state.value.activeReel)

        // Open reel
        viewModel.onEvent(ReelHighlightEvent.OpenReel(TimeOfDayFilter.MORNING))
        advanceUntilIdle()

        val reel = viewModel.state.value.activeReel
        assertNotNull(reel)
        assertEquals(0, reel.currentSlideIndex)
        assertEquals(2, reel.slides.size)

        // Next slide
        viewModel.onEvent(ReelHighlightEvent.NextSlide)
        assertEquals(1, viewModel.state.value.activeReel?.currentSlideIndex)

        // Previous slide
        viewModel.onEvent(ReelHighlightEvent.PreviousSlide)
        assertEquals(0, viewModel.state.value.activeReel?.currentSlideIndex)

        // Advance past end should close reel
        viewModel.onEvent(ReelHighlightEvent.NextSlide)
        viewModel.onEvent(ReelHighlightEvent.NextSlide)
        assertNull(viewModel.state.value.activeReel)
    }

    @Test
    fun testReelPauseAndTtsEvents() = runTest(testDispatcher) {
        val viewModel = ReelHighlightViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.onEvent(ReelHighlightEvent.OpenReel(TimeOfDayFilter.MORNING))
        advanceUntilIdle()

        viewModel.onEvent(ReelHighlightEvent.SetReelPaused(true))
        assertTrue(viewModel.state.value.activeReel!!.isPaused)

        viewModel.onEvent(ReelHighlightEvent.ToggleTts)
        assertTrue(viewModel.state.value.activeReel!!.isTtsSpeaking)

        viewModel.onEvent(ReelHighlightEvent.ToggleTicketShare(true))
        assertTrue(viewModel.state.value.activeReel!!.showTicketShare)
    }
}
