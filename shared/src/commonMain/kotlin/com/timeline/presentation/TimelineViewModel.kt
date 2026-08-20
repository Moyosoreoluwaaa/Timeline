package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.Session
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock as KClock
import kotlin.time.Duration.Companion.minutes

class TimelineViewModel(
    private val repository: TimelineRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    private val _effects = Channel<TimelineEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        onEvent(TimelineEvent.Refresh)
    }

    fun onEvent(event: TimelineEvent) {
        Logger.d { "TimelineViewModel onEvent: $event" }
        when (event) {
            is TimelineEvent.Refresh -> loadTimeline()
            is TimelineEvent.GenerateDummyData -> generateDummyData()
            is TimelineEvent.SelectDate -> _state.update { it.copy(selectedDate = event.date) }
            is TimelineEvent.SelectSession -> _state.update { it.copy(selectedSession = event.session) }
            is TimelineEvent.ToggleSheet -> _state.update { it.copy(isSheetExpanded = event.expanded) }
            is TimelineEvent.FilterTime -> _state.update { it.copy(timeFilter = event.filter) }
        }
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getTimeline()
                .onEach { sessions ->
                    _state.update { it.copy(sessions = sessions, isLoading = false) }
                }
                .catch { e ->
                    Logger.e(e) { "Failed to load timeline" }
                    _state.update { it.copy(isLoading = false) }
                    _effects.send(TimelineEffect.ShowError(e.message ?: "Unknown error"))
                }
                .collect()
        }
    }

    private fun generateDummyData() {
        viewModelScope.launch {
            val apps = listOf("com.android.chrome", "com.google.android.youtube", "com.whatsapp", "com.instagram.android")
            val now = KClock.System.now()
            
            apps.forEachIndexed { index, pkg ->
                val session = Session(
                    id = "dummy_${index}_${Random.nextInt()}",
                    packageName = pkg,
                    startTime = now.minus((index * 30).minutes),
                    endTime = now.minus((index * 30 - 15).minutes),
                    durationMinutes = 15,
                    screenshots = emptyList(),
                    segments = emptyList()
                )
                repository.saveSession(session)
            }
            onEvent(TimelineEvent.Refresh)
        }
    }
}
