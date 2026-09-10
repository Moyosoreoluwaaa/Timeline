package com.timeline.presentation

import kotlin.time.Instant

data class NewHighlightState(
    val timeOfDayFilter: TimeOfDayFilter = TimeOfDayFilter.ALL,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedDate: Instant? = null,
    val narrativeText: String = "Today started with a calm morning review of communication apps and quick message check-ins. By afternoon, productivity peaked with deep coding sessions, IDE navigation, and documentation referencing. Evening wind-down involved media streaming, relaxing web browsing, and organizing tasks for tomorrow.",
    val dynamicScreenshots: List<String> = emptyList(),
    val sampleThumbnailsLeft: List<String> = emptyList(),
    val sampleThumbnailsRight: List<String> = emptyList()
)

enum class TimeOfDayFilter {
    ALL, MORNING, AFTERNOON, EVENING
}

sealed interface NewHighlightEvent {
    data class SetFilter(val filter: TimeOfDayFilter) : NewHighlightEvent
    data object Refresh : NewHighlightEvent
}
