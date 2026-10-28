package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.repository.ReelHighlightRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Reel Highlight experience.
 *
 * Manages:
 * - Live segment list (from [ReelHighlightRepository.getDaySegments])
 * - Reel playback state ([ReelPlaybackState]) including auto-advance timer
 * - Sync / refresh triggers
 */
class ReelHighlightViewModel(
    private val repository: ReelHighlightRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReelHighlightState())
    val state: StateFlow<ReelHighlightState> = _state.asStateFlow()

    private var segmentsJob: Job? = null

    init {
        loadSegments()
    }

    fun onEvent(event: ReelHighlightEvent) {
        when (event) {
            is ReelHighlightEvent.SetFilter ->
                _state.update { it.copy(timeOfDayFilter = event.filter) }

            is ReelHighlightEvent.Refresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    delay(400.milliseconds)
                    loadSegments()
                    _state.update { it.copy(isRefreshing = false) }
                }
            }

            is ReelHighlightEvent.SyncRealData -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSyncing = true) }
                    repository.syncRealDeviceUsage()
                    loadSegments()
                    _state.update { it.copy(isSyncing = false) }
                }
            }

            is ReelHighlightEvent.ToggleDevDrawer ->
                _state.update { it.copy(isDevDrawerOpen = event.open) }

            is ReelHighlightEvent.OpenReel -> openReel(event.filter)
            is ReelHighlightEvent.CloseReel -> closeReel()
            is ReelHighlightEvent.NextSlide -> advanceSlide()
            is ReelHighlightEvent.PreviousSlide -> previousSlide()

            is ReelHighlightEvent.SetReelPaused ->
                _state.update { current ->
                    current.copy(activeReel = current.activeReel?.copy(isPaused = event.paused))
                }

            is ReelHighlightEvent.ToggleTts ->
                _state.update { current ->
                    val speaking = !(current.activeReel?.isTtsSpeaking ?: false)
                    current.copy(activeReel = current.activeReel?.copy(isTtsSpeaking = speaking))
                }

            is ReelHighlightEvent.ToggleTicketShare ->
                _state.update { current ->
                    current.copy(activeReel = current.activeReel?.copy(showTicketShare = event.show))
                }
        }
    }

    // ---------- Private helpers ----------

    private fun loadSegments() {
        segmentsJob?.cancel()
        segmentsJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getDaySegments().collect { segments ->
                val tz = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDateTime(tz).date
                val dateLabel = "${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.day}, ${today.year}"
                _state.update { current ->
                    current.copy(
                        segments = segments,
                        selectedDateLabel = dateLabel,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun openReel(filter: TimeOfDayFilter) {
        viewModelScope.launch {
            val slides = repository.getReelSlides(filter)
            if (slides.isNotEmpty()) {
                val title = _state.value.segments
                    .firstOrNull { it.filter == filter }
                    ?.title ?: filter.name.lowercase().replaceFirstChar { it.uppercase() }
                _state.update {
                    it.copy(
                        activeReel = ReelPlaybackState(
                            filter = filter,
                            segmentTitle = title,
                            slides = slides,
                            currentSlideIndex = 0,
                            isPaused = false,
                            isTtsSpeaking = false,
                            showTicketShare = false
                        )
                    )
                }
            }
        }
    }

    private fun closeReel() {
        _state.update { it.copy(activeReel = null) }
    }

    private fun advanceSlide() {
        val reel = _state.value.activeReel ?: return
        if (reel.currentSlideIndex < reel.slides.size - 1) {
            _state.update { current ->
                current.copy(
                    activeReel = current.activeReel?.copy(
                        currentSlideIndex = reel.currentSlideIndex + 1
                    )
                )
            }
        } else {
            closeReel()
        }
    }

    private fun previousSlide() {
        val reel = _state.value.activeReel ?: return
        if (reel.currentSlideIndex > 0) {
            _state.update { current ->
                current.copy(
                    activeReel = current.activeReel?.copy(
                        currentSlideIndex = reel.currentSlideIndex - 1
                    )
                )
            }
        }
    }
}
