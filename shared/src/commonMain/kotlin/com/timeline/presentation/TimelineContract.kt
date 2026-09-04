package com.timeline.presentation

import com.timeline.domain.Session
import kotlin.time.Instant

data class TimelineState(
    val sessions: List<Session> = emptyList(),
    val summary: TimelineSummary = TimelineSummary(),
    val isLoading: Boolean = false,
    val selectedDate: Instant? = null,
    val selectedPackageName: String? = null,
    val selectedSession: Session? = null,
    val relatedSessions: List<Session> = emptyList(), // Added for detailed view
    val fullScreenImagePath: String? = null, // Added for image preview
    val isSheetExpanded: Boolean = false,
    val timeFilter: TimeFilter = TimeFilter.ALL
)

data class TimelineSummary(
    val totalHours: Long = 0,
    val totalMinutes: Long = 0,
    val sessionCount: Int = 0,
    val mostUsedApps: List<AppSummary> = emptyList()
)

data class AppSummary(
    val packageName: String,
    val displayName: String?,
    val icon: Any?,
    val totalTimeMinutes: Long
)

enum class TimeFilter {
    ALL, MORNING, AFTERNOON, EVENING
}

sealed interface TimelineEvent {
    data object Refresh : TimelineEvent
    data object GenerateDummyData : TimelineEvent
    data class SelectDate(val date: Instant) : TimelineEvent
    data class SelectSession(val session: Session?) : TimelineEvent
    data class ShowFullScreenImage(val path: String?) : TimelineEvent
    data object DismissFullScreenImage : TimelineEvent
    data class ToggleSheet(val expanded: Boolean) : TimelineEvent
    data class FilterTime(val filter: TimeFilter) : TimelineEvent
    data class SelectPackage(val packageName: String?) : TimelineEvent // Added for deep-dive
    data object SelectPreviousSession : TimelineEvent
    data object SelectNextSession : TimelineEvent
}

sealed interface TimelineEffect {
    data class ShowError(val message: String) : TimelineEffect
    data class NavigateToSettings(val dummy: Unit = Unit) : TimelineEffect
}
