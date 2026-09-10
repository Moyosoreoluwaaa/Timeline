package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.data.TimelineRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class NewHighlightViewModel(
    private val repository: TimelineRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NewHighlightState())
    val state: StateFlow<NewHighlightState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getTimeline().collect { sessions ->
                val allPaths = sessions.flatMap { s -> s.screenshots + s.segments.mapNotNull { it.screenshotPath } }.distinct()
                val left = allPaths.take(3)
                val right = allPaths.drop(3).take(3).ifEmpty { left }
                
                _state.update {
                    it.copy(
                        dynamicScreenshots = allPaths,
                        sampleThumbnailsLeft = left,
                        sampleThumbnailsRight = right
                    )
                }
            }
        }
    }

    fun onEvent(event: NewHighlightEvent) {
        when (event) {
            is NewHighlightEvent.SetFilter -> {
                _state.update { it.copy(timeOfDayFilter = event.filter) }
            }
            is NewHighlightEvent.Refresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    delay(2000.milliseconds)
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }
}
