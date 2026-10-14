package com.timeline.presentation

import com.timeline.domain.AppMetadata
import com.timeline.domain.reasoning.HighlightReasoningMode
import kotlin.time.Instant

/**
 * Segment model representing an aggregated time block (e.g. morning, afternoon, evening) in executive highlights.
 */
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

/**
 * Filter enum for segregating activity into time-of-day blocks.
 */
enum class TimeOfDayFilter {
    ALL, MORNING, AFTERNOON, EVENING
}

/**
 * Polished, user-facing executive highlight state summarizing activity into morning/afternoon/evening segments.
 */
data class SimplifiedHighlightState(
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
    val reasoningMode: HighlightReasoningMode = HighlightReasoningMode.BALANCED,
    val selectedAppPackage: String? = null,
    val topApps: List<AppMetadata> = emptyList()
)

/**
 * Events accepted by SimplifiedHighlightViewModel.
 */
sealed interface SimplifiedHighlightEvent {
    data class SetFilter(val filter: TimeOfDayFilter) : SimplifiedHighlightEvent
    data object Refresh : SimplifiedHighlightEvent
    data object SyncRealData : SimplifiedHighlightEvent
    data class SelectDate(val date: Instant?) : SimplifiedHighlightEvent
    data class PreviewScreenshot(val path: String?) : SimplifiedHighlightEvent
    data class SetReasoningMode(val mode: HighlightReasoningMode) : SimplifiedHighlightEvent
    data class SelectApp(val packageName: String?) : SimplifiedHighlightEvent
}
