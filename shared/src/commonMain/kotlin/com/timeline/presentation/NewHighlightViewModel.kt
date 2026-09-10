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
            // Load sample screenshot paths from repository for the loading state animation
            repository.getTimeline().collect { sessions ->
                val allPaths = sessions.flatMap { s -> s.screenshots + s.segments.mapNotNull { it.screenshotPath } }.distinct()
                _state.update {
                    it.copy(
                        sampleThumbnailsLeft = allPaths.take(5)
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
            is NewHighlightEvent.TriggerRefresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    delay(3000)
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
