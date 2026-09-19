package com.timeline.presentation

import kotlin.time.Instant

data class HighlightSegment(
    val filter: TimeOfDayFilter,
    val title: String,
    val timeRange: String,
    val narrative: String,
    val apps: List<String>,
    val duration: String,
    val screenshots: List<String> = emptyList(),
    val actionItems: List<String> = emptyList()
)

data class NewHighlightState(
    val timeOfDayFilter: TimeOfDayFilter = TimeOfDayFilter.ALL,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val isUsagePermissionGranted: Boolean = true,
    val selectedDate: Instant? = null,
    val dynamicScreenshots: List<String> = emptyList(),
    val segments: List<HighlightSegment> = emptyList(),
    val overallActionItems: List<String> = emptyList(),
    val previewingScreenshotPath: String? = null,
    val reasoningMode: com.timeline.domain.reasoning.HighlightReasoningMode = com.timeline.domain.reasoning.HighlightReasoningMode.BALANCED,
    val selectedAppPackage: String? = null,
    val topApps: List<com.timeline.domain.AppMetadata> = emptyList()
)

enum class TimeOfDayFilter {
    ALL, MORNING, AFTERNOON, EVENING
}

sealed interface NewHighlightEvent {
    data class SetFilter(val filter: TimeOfDayFilter) : NewHighlightEvent
    data object Refresh : NewHighlightEvent
    data object SyncRealData : NewHighlightEvent
    data class SelectDate(val date: Instant?) : NewHighlightEvent
    data class PreviewScreenshot(val path: String?) : NewHighlightEvent
    data class SetReasoningMode(val mode: com.timeline.domain.reasoning.HighlightReasoningMode) : NewHighlightEvent
    data class SelectApp(val packageName: String?) : NewHighlightEvent
}
