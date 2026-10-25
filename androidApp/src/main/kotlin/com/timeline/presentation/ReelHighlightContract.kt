package com.timeline.presentation

import com.timeline.domain.model.ReelSegment
import com.timeline.domain.model.ReelSlide

// ---------------------------------------------------------------------------
// Reel state
// ---------------------------------------------------------------------------

/**
 * State for the active story reel (full-screen slide playback).
 */
data class ReelPlaybackState(
    val filter: TimeOfDayFilter = TimeOfDayFilter.AFTERNOON,
    val segmentTitle: String = "",
    val slides: List<ReelSlide> = emptyList(),
    val currentSlideIndex: Int = 0,
    val isPaused: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val showTicketShare: Boolean = false
) {
    val currentSlide: ReelSlide?
        get() = slides.getOrNull(currentSlideIndex)

    val progress: Float
        get() = if (slides.isNotEmpty())
            (currentSlideIndex + 1).toFloat() / slides.size.toFloat()
        else 0f
}

/**
 * Full UI state for the reel-highlight screen.
 */
data class ReelHighlightState(
    val timeOfDayFilter: TimeOfDayFilter = TimeOfDayFilter.ALL,
    val selectedDateLabel: String = "",
    val totalUsageFormatted: String = "",
    val segments: List<ReelSegment> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val isDevDrawerOpen: Boolean = false,
    val activeReel: ReelPlaybackState? = null
)

// ---------------------------------------------------------------------------
// Events
// ---------------------------------------------------------------------------

sealed interface ReelHighlightEvent {
    // List-screen events
    data class SetFilter(val filter: TimeOfDayFilter) : ReelHighlightEvent
    data object Refresh : ReelHighlightEvent
    data object SyncRealData : ReelHighlightEvent
    data class ToggleDevDrawer(val open: Boolean) : ReelHighlightEvent

    // Reel playback events
    data class OpenReel(val filter: TimeOfDayFilter) : ReelHighlightEvent
    data object CloseReel : ReelHighlightEvent
    data object NextSlide : ReelHighlightEvent
    data object PreviousSlide : ReelHighlightEvent
    data class SetReelPaused(val paused: Boolean) : ReelHighlightEvent
    data object ToggleTts : ReelHighlightEvent
    data class ToggleTicketShare(val show: Boolean) : ReelHighlightEvent
}
