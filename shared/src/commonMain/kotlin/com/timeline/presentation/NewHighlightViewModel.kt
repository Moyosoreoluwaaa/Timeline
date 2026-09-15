package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class NewHighlightViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val deviceUsageSyncer: DeviceUsageSyncer,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(
        NewHighlightState(
            isUsagePermissionGranted = deviceUsageSyncer.hasPermission()
        )
    )
    val state: StateFlow<NewHighlightState> = _state.asStateFlow()

    private var timelineJob: Job? = null

    init {
        checkPermissionAndLoadData()
    }

    fun checkPermissionAndLoadData() {
        val hasPerm = deviceUsageSyncer.hasPermission()
        _state.update { it.copy(isUsagePermissionGranted = hasPerm) }
        loadTimelineData()
    }

    private fun loadTimelineData() {
        timelineJob?.cancel()
        timelineJob = viewModelScope.launch {
            repository.getTimeline().collect { sessions ->
                processSessionsIntoSegments(sessions)
            }
        }
    }

    private suspend fun processSessionsIntoSegments(sessions: List<Session>) {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val todayLocalDate = now.toLocalDateTime(tz).date

        var targetDate = _state.value.selectedDate ?: now
        var targetLocalDate = targetDate.toLocalDateTime(tz).date

        // Filter sessions for selected date
        var sessionsForDate = sessions.filter { it.startTime.toLocalDateTime(tz).date == targetLocalDate }

        // Auto-select date with sessions if selected date has none and is today
        if (sessionsForDate.isEmpty() && sessions.isNotEmpty() && targetLocalDate == todayLocalDate) {
            val latestSession = sessions.maxByOrNull { it.startTime }
            if (latestSession != null) {
                targetDate = latestSession.startTime
                targetLocalDate = targetDate.toLocalDateTime(tz).date
                sessionsForDate = sessions.filter { it.startTime.toLocalDateTime(tz).date == targetLocalDate }
            }
        }

        val allScreenshotPaths = sessionsForDate.flatMap { s ->
            s.screenshots + s.segments.mapNotNull { it.screenshotPath }
        }.distinct()

        // Categorize into Morning (6am - 12pm), Afternoon (12pm - 5pm), Evening (5pm - 6am)
        val morningSessions = sessionsForDate.filter { s ->
            val hour = s.startTime.toLocalDateTime(tz).hour
            hour in 6..11
        }
        val afternoonSessions = sessionsForDate.filter { s ->
            val hour = s.startTime.toLocalDateTime(tz).hour
            hour in 12..16
        }
        val eveningSessions = sessionsForDate.filter { s ->
            val hour = s.startTime.toLocalDateTime(tz).hour
            hour in 17..23 || hour in 0..5
        }

        // Fetch overall daily reasoning if available
        val dateString = targetLocalDate.toString()
        val dailyReasoning = repository.getAppDailyReasoning("ALL_APPS", dateString).firstOrNull()

        val morningSegment = buildSegment(
            filter = TimeOfDayFilter.MORNING,
            title = "Morning Digest",
            timeRange = "6:00 AM - 12:00 PM",
            sessions = morningSessions,
            savedReasoning = dailyReasoning
        )

        val afternoonSegment = buildSegment(
            filter = TimeOfDayFilter.AFTERNOON,
            title = "Afternoon Digest",
            timeRange = "12:00 PM - 5:00 PM",
            sessions = afternoonSessions,
            savedReasoning = dailyReasoning
        )

        val eveningSegment = buildSegment(
            filter = TimeOfDayFilter.EVENING,
            title = "Evening Digest",
            timeRange = "5:00 PM - 10:00 PM",
            sessions = eveningSessions,
            savedReasoning = dailyReasoning
        )

        val actionItems = dailyReasoning?.actionItems
            ?: (morningSegment.actionItems + afternoonSegment.actionItems + eveningSegment.actionItems).distinct()

        _state.update { current ->
            current.copy(
                selectedDate = targetDate,
                dynamicScreenshots = allScreenshotPaths,
                segments = listOf(morningSegment, afternoonSegment, eveningSegment),
                overallActionItems = actionItems,
                isLoading = false
            )
        }
    }

    private suspend fun buildSegment(
        filter: TimeOfDayFilter,
        title: String,
        timeRange: String,
        sessions: List<Session>,
        savedReasoning: com.timeline.domain.reasoning.AppDailyReasoning?
    ): HighlightSegment {
        if (sessions.isEmpty()) {
            val emptyNarrative = when (filter) {
                TimeOfDayFilter.MORNING -> "A quiet morning with minimal device screen activity. Time was well spent offline."
                TimeOfDayFilter.AFTERNOON -> "No active digital workflows recorded during the afternoon hours."
                TimeOfDayFilter.EVENING -> "A peaceful evening with little to no screen interaction."
                else -> "No screen activity recorded for this period."
            }
            return HighlightSegment(
                filter = filter,
                title = title,
                timeRange = timeRange,
                narrative = emptyNarrative,
                apps = emptyList(),
                duration = "Inactive",
                screenshots = emptyList(),
                actionItems = emptyList()
            )
        }

        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val durationFormatted = formatDuration(totalMinutes)

        val appNamesList = mutableListOf<String>()
        for (session in sessions) {
            val name = session.displayName ?: appInfoProvider.getAppName(session.packageName)
            appNamesList.add(name)
        }
        val appNames = appNamesList.distinct()

        val screenshots = sessions.flatMap { session ->
            session.screenshots + session.segments.mapNotNull { it.screenshotPath }
        }.distinct()

        val topAppsText = if (appNames.isNotEmpty()) {
            appNames.take(4).joinToString(", ")
        } else {
            "various tools"
        }

        val periodName = when (filter) {
            TimeOfDayFilter.MORNING -> "morning"
            TimeOfDayFilter.AFTERNOON -> "afternoon"
            TimeOfDayFilter.EVENING -> "evening"
            else -> "day"
        }

        val defaultNarrative = when (filter) {
            TimeOfDayFilter.MORNING -> "Started the $periodName with active engagements across $topAppsText. Focused on priority check-ins and kickstarting key tasks."
            TimeOfDayFilter.AFTERNOON -> "Peak productivity during the $periodName navigating $topAppsText. Accomplished workflow milestones and document reviews."
            TimeOfDayFilter.EVENING -> "Wind-down during the $periodName using $topAppsText. Wrapped up loose ends, caught up on updates, and prepared for tomorrow."
            else -> "Active usage recorded across $topAppsText."
        }

        val narrative = savedReasoning?.summary ?: defaultNarrative
        val actionItems = savedReasoning?.actionItems ?: emptyList()

        return HighlightSegment(
            filter = filter,
            title = title,
            timeRange = timeRange,
            narrative = narrative,
            apps = appNames,
            duration = durationFormatted,
            screenshots = screenshots,
            actionItems = actionItems
        )
    }

    private fun formatDuration(minutes: Long): String {
        return if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            if (m > 0) "${h}h ${m}m active" else "${h}h active"
        } else {
            "${minutes}m active"
        }
    }

    fun onEvent(event: NewHighlightEvent) {
        when (event) {
            is NewHighlightEvent.SetFilter -> {
                _state.update { it.copy(timeOfDayFilter = event.filter) }
            }
            is NewHighlightEvent.Refresh -> {
                syncRealData()
            }
            is NewHighlightEvent.SyncRealData -> {
                syncRealData()
            }
            is NewHighlightEvent.SelectDate -> {
                _state.update { it.copy(selectedDate = event.date, isLoading = true) }
                loadTimelineData()
            }
            is NewHighlightEvent.PreviewScreenshot -> {
                _state.update { it.copy(previewingScreenshotPath = event.path) }
            }
        }
    }

    private fun syncRealData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, isRefreshing = true) }
            try {
                val syncedCount = deviceUsageSyncer.syncRealDeviceUsage(daysBack = 2)
                logger.i { "NewHighlightViewModel: Synced $syncedCount real device usage sessions" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to sync device usage" }
            } finally {
                _state.update { it.copy(isSyncing = false, isRefreshing = false) }
                loadTimelineData()
            }
        }
    }
}
