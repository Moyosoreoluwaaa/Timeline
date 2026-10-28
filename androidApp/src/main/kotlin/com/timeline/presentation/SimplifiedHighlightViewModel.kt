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

/**
 * ViewModel managing the polished, high-level executive highlight narratives and time-of-day segments.
 */
class SimplifiedHighlightViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val deviceUsageSyncer: DeviceUsageSyncer,
    logger: Logger
) : ViewModel() {
    private val logger = logger.withTag("SimplifiedHighlightViewModel")

    private val _state = MutableStateFlow(
        SimplifiedHighlightState(
            isUsagePermissionGranted = deviceUsageSyncer.hasPermission(),
            isLoading = true
        )
    )
    val state: StateFlow<SimplifiedHighlightState> = _state.asStateFlow()

    private var timelineJob: Job? = null

    init {
        logger.i { "Initializing SimplifiedHighlightViewModel" }
        checkPermissionAndLoadData()
    }

    fun checkPermissionAndLoadData() {
        val hasPerm = deviceUsageSyncer.hasPermission()
        _state.update { it.copy(isUsagePermissionGranted = hasPerm) }
        loadTimelineData()
    }

    private fun loadTimelineData() {
        timelineJob?.cancel()
        _state.update { it.copy(isLoading = true) }
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

        var sessionsForDate = sessions.filter { it.startTime.toLocalDateTime(tz).date == targetLocalDate }

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

        val uniqueApps = sessionsForDate.map { s ->
            com.timeline.domain.AppMetadata(
                packageName = s.packageName,
                name = s.displayName ?: appInfoProvider.getAppName(s.packageName),
                icon = s.icon ?: appInfoProvider.getAppIcon(s.packageName)
            )
        }.distinctBy { it.packageName }.take(3)

        val morningSessions = sessionsForDate.filter { s ->
            s.startTime.toLocalDateTime(tz).hour in 6..11
        }
        val afternoonSessions = sessionsForDate.filter { s ->
            s.startTime.toLocalDateTime(tz).hour in 12..16
        }
        val eveningSessions = sessionsForDate.filter { s ->
            val hour = s.startTime.toLocalDateTime(tz).hour
            hour in 17..23 || hour in 0..5
        }

        val dateString = targetLocalDate.toString()
        val morningReasoning = repository.getAppDailyReasoning("ALL_APPS_MORNING", dateString).firstOrNull()
        val afternoonReasoning = repository.getAppDailyReasoning("ALL_APPS_AFTERNOON", dateString).firstOrNull()
        val eveningReasoning = repository.getAppDailyReasoning("ALL_APPS_EVENING", dateString).firstOrNull()
        val dailyReasoning = repository.getAppDailyReasoning("ALL_APPS", dateString).firstOrNull()

        val morningSegment = HighlightSegmentBuilder.buildSegment(
            filter = TimeOfDayFilter.MORNING,
            title = "Morning Digest",
            timeRange = "6:00 AM - 12:00 PM",
            sessions = morningSessions,
            savedReasoning = morningReasoning,
            appInfoProvider = appInfoProvider,
            tz = tz
        )

        val afternoonSegment = HighlightSegmentBuilder.buildSegment(
            filter = TimeOfDayFilter.AFTERNOON,
            title = "Afternoon Digest",
            timeRange = "12:00 PM - 5:00 PM",
            sessions = afternoonSessions,
            savedReasoning = afternoonReasoning,
            appInfoProvider = appInfoProvider,
            tz = tz
        )

        val eveningSegment = HighlightSegmentBuilder.buildSegment(
            filter = TimeOfDayFilter.EVENING,
            title = "Evening Digest",
            timeRange = "5:00 PM - 10:00 PM",
            sessions = eveningSessions,
            savedReasoning = eveningReasoning ?: dailyReasoning,
            appInfoProvider = appInfoProvider,
            tz = tz
        )

        val actionItems = dailyReasoning?.actionItems?.ifEmpty { null }
            ?: (morningSegment.actionItems + afternoonSegment.actionItems + eveningSegment.actionItems).distinct()

        _state.update { current ->
            current.copy(
                selectedDate = targetDate,
                dynamicScreenshots = allScreenshotPaths,
                segments = listOf(morningSegment, afternoonSegment, eveningSegment),
                overallActionItems = actionItems,
                topApps = uniqueApps,
                isLoading = false
            )
        }
    }

    fun onEvent(event: SimplifiedHighlightEvent) {
        when (event) {
            is SimplifiedHighlightEvent.SetFilter -> {
                _state.update { it.copy(timeOfDayFilter = event.filter) }
            }
            is SimplifiedHighlightEvent.Refresh -> {
                loadTimelineData()
            }
            is SimplifiedHighlightEvent.SyncRealData -> {
                syncRealData()
            }
            is SimplifiedHighlightEvent.SelectDate -> {
                _state.update { it.copy(selectedDate = event.date) }
                loadTimelineData()
            }
            is SimplifiedHighlightEvent.PreviewScreenshot -> {
                _state.update { it.copy(previewingScreenshotPath = event.path) }
            }
            is SimplifiedHighlightEvent.SetReasoningMode -> {
                _state.update { it.copy(reasoningMode = event.mode) }
            }
            is SimplifiedHighlightEvent.SelectApp -> {
                _state.update { it.copy(selectedAppPackage = event.packageName) }
            }
        }
    }

    private fun syncRealData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            val syncedCount = deviceUsageSyncer.syncRealDeviceUsage()
            logger.i { "SimplifiedHighlightViewModel: Synced $syncedCount device usage sessions" }
            _state.update { it.copy(isSyncing = false) }
            loadTimelineData()
        }
    }
}
