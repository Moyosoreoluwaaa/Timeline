package com.timeline.presentation

import kotlin.time.Instant

data class HighlightSegment(
    val filter: TimeOfDayFilter,
    val title: String,
    val timeRange: String,
    val narrative: String,
    val apps: List<String>,
    val duration: String,
    val screenshots: List<String> = emptyList()
)

data class NewHighlightState(
    val timeOfDayFilter: TimeOfDayFilter = TimeOfDayFilter.ALL,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedDate: Instant? = null,
    val narrativeText: String = "Today started with a calm morning review of communication apps and quick message check-ins. By afternoon, productivity peaked with deep coding sessions, IDE navigation, and documentation referencing. Evening wind-down involved media streaming, relaxing web browsing, and organizing tasks for tomorrow.",
    val dynamicScreenshots: List<String> = emptyList(),
    val sampleThumbnailsLeft: List<String> = emptyList(),
    val sampleThumbnailsRight: List<String> = emptyList(),
    val segments: List<HighlightSegment> = listOf(
        HighlightSegment(
            filter = TimeOfDayFilter.MORNING,
            title = "Morning Digest",
            timeRange = "6:00 AM - 12:00 PM",
            narrative = "Today started with a calm morning review of communication apps, quick message check-ins, and setting priorities for the day.",
            apps = listOf("Slack", "Gmail", "Calendar"),
            duration = "2h 15m active"
        ),
        HighlightSegment(
            filter = TimeOfDayFilter.AFTERNOON,
            title = "Afternoon Digest",
            timeRange = "12:00 PM - 5:00 PM",
            narrative = "By afternoon, productivity peaked with deep coding sessions, IDE navigation, terminal executions, and documentation referencing.",
            apps = listOf("Android Studio", "Terminal", "Chrome", "GitHub"),
            duration = "4h 10m active"
        ),
        HighlightSegment(
            filter = TimeOfDayFilter.EVENING,
            title = "Evening Digest",
            timeRange = "5:00 PM - 10:00 PM",
            narrative = "Evening wind-down involved media streaming, relaxing web browsing, organizing tasks, and taking notes for tomorrow.",
            apps = listOf("YouTube", "Reddit", "Notion", "Spotify"),
            duration = "1h 45m active"
        )
    )
)

enum class TimeOfDayFilter {
    ALL, MORNING, AFTERNOON, EVENING
}

sealed interface NewHighlightEvent {
    data class SetFilter(val filter: TimeOfDayFilter) : NewHighlightEvent
    data object Refresh : NewHighlightEvent
}
