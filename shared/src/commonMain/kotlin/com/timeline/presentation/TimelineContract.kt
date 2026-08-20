package com.timeline.presentation

import com.timeline.domain.Session
import kotlinx.datetime.Instant

data class TimelineState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val selectedDate: Instant? = null,
    val selectedSession: Session? = null,
    val isSheetExpanded: Boolean = false,
    val timeFilter: TimeFilter = TimeFilter.ALL
)

enum class TimeFilter {
    ALL, MORNING, AFTERNOON, EVENING
}

sealed interface TimelineEvent {
    data object Refresh : TimelineEvent
    data object GenerateDummyData : TimelineEvent
    data class SelectDate(val date: Instant) : TimelineEvent
    data class SelectSession(val session: Session?) : TimelineEvent
    data class ToggleSheet(val expanded: Boolean) : TimelineEvent
    data class FilterTime(val filter: TimeFilter) : TimelineEvent
    data object SelectPreviousSession : TimelineEvent
    data object SelectNextSession : TimelineEvent
}

sealed interface TimelineEffect {
    data class ShowError(val message: String) : TimelineEffect
    data class NavigateToSettings(val dummy: Unit = Unit) : TimelineEffect
}
