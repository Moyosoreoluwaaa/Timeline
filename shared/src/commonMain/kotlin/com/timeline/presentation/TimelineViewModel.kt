package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.Session
import com.timeline.util.PlaceholderData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val exclusionPolicy: ExclusionPolicy
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _selectedDate = MutableStateFlow<Instant>(Clock.System.now())
    private val _selectedPackageName = MutableStateFlow<String?>(null)
    private val _selectedSession = MutableStateFlow<Session?>(null)
    private val _fullScreenImagePath = MutableStateFlow<String?>(null)
    private val _isSheetExpanded = MutableStateFlow(false)
    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<TimelineState> = combine(
        repository.getTimeline(),
        exclusionPolicy.getExcludedPackages(),
        _selectedDate,
        _selectedPackageName,
        _selectedSession,
        _fullScreenImagePath,
        _isSheetExpanded,
        _timeFilter,
        _isLoading
    ) { args: Array<Any?> ->
        val sessions = args[0] as List<Session>
        val excluded = args[1] as Set<String>
        val date = args[2] as Instant
        val packageName = args[3] as String?
        val session = args[4] as Session?
        val fullScreenImage = args[5] as String?
        val expanded = args[6] as Boolean
        val filter = args[7] as TimeFilter
        val loading = args[8] as Boolean

        val filteredSessions = filterSessions(sessions, excluded, date, filter, packageName)
        val summary = calculateSummary(filteredSessions)
        val related = if (session != null) {
            sessions.filter { it.packageName == session.packageName && applyDateFilter(it, date) }
        } else emptyList()

        TimelineState(
            sessions = filteredSessions,
            summary = summary,
            isLoading = loading,
            selectedDate = date,
            selectedPackageName = packageName,
            selectedSession = session,
            relatedSessions = related,
            fullScreenImagePath = fullScreenImage,
            isSheetExpanded = expanded,
            timeFilter = filter
        )
    }.flatMapLatest { s ->
        flow {
            val enrichedSessions = s.sessions.map { session ->
                if (session.displayName == null) {
                    session.copy(
                        displayName = appInfoProvider.getAppName(session.packageName),
                        icon = appInfoProvider.getAppIcon(session.packageName)
                    )
                } else session
            }
            val enrichedRelated = s.relatedSessions.map { session ->
                if (session.displayName == null) {
                    session.copy(
                        displayName = appInfoProvider.getAppName(session.packageName),
                        icon = appInfoProvider.getAppIcon(session.packageName)
                    )
                } else session
            }
            val enrichedSelected = s.selectedSession?.let { session ->
                if (session.displayName == null) {
                    session.copy(
                        displayName = appInfoProvider.getAppName(session.packageName),
                        icon = appInfoProvider.getAppIcon(session.packageName)
                    )
                } else session
            }

            val enrichedSummary = s.summary.copy(
                mostUsedApps = s.summary.mostUsedApps.map { app ->
                    app.copy(
                        displayName = app.displayName ?: appInfoProvider.getAppName(app.packageName),
                        icon = app.icon ?: appInfoProvider.getAppIcon(app.packageName)
                    )
                }
            )
            emit(s.copy(
                sessions = enrichedSessions,
                relatedSessions = enrichedRelated,
                selectedSession = enrichedSelected,
                summary = enrichedSummary
            ))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimelineState())

    private val _effects = Channel<TimelineEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: TimelineEvent) {
        Logger.d { "TimelineViewModel onEvent: $event" }
        when (event) {
            is TimelineEvent.Refresh -> _refreshTrigger.value++
            is TimelineEvent.GenerateDummyData -> generateDummyData()
            is TimelineEvent.SelectDate -> _selectedDate.value = event.date
            is TimelineEvent.SelectPackage -> _selectedPackageName.value = event.packageName
            is TimelineEvent.SelectSession -> _selectedSession.value = event.session
            is TimelineEvent.ShowFullScreenImage -> _fullScreenImagePath.value = event.path
            is TimelineEvent.DismissFullScreenImage -> _fullScreenImagePath.value = null
            is TimelineEvent.ToggleSheet -> _isSheetExpanded.value = event.expanded
            is TimelineEvent.FilterTime -> _timeFilter.value = event.filter
            is TimelineEvent.SelectPreviousSession -> navigateSession(-1)
            is TimelineEvent.SelectNextSession -> navigateSession(1)
        }
    }

    private fun filterSessions(
        sessions: List<Session>,
        excluded: Set<String>,
        date: Instant,
        filter: TimeFilter,
        packageName: String?
    ): List<Session> {
        return sessions
            .filter { it.packageName !in excluded }
            .filter { applyDateFilter(it, date) }
            .filter { applyTimeFilter(it, filter) }
            .filter { packageName == null || it.packageName == packageName }
    }

    private fun calculateSummary(sessions: List<Session>): TimelineSummary {
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val mostUsed = sessions
            .groupBy { it.packageName }
            .map { (pkg, appSessions) ->
                AppSummary(
                    packageName = pkg,
                    displayName = appSessions.firstOrNull()?.displayName,
                    icon = appSessions.firstOrNull()?.icon,
                    totalTimeMinutes = appSessions.sumOf { it.durationMinutes }
                )
            }
            .sortedByDescending { it.totalTimeMinutes }
            .take(3)

        return TimelineSummary(
            totalHours = totalMinutes / 60,
            totalMinutes = totalMinutes % 60,
            sessionCount = sessions.size,
            mostUsedApps = mostUsed
        )
    }

    /**
     * Future-proofing: Groups usage by hour segments for stacked charts or flow charts.
     */
    private fun groupUsageByTime(sessions: List<Session>): Map<Int, List<Session>> {
        return sessions.groupBy {
            it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour
        }
    }

    private fun navigateSession(direction: Int) {
        val sessions = state.value.sessions
        val current = state.value.selectedSession ?: return
        val currentIndex = sessions.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val nextIndex = currentIndex + direction
            if (nextIndex in sessions.indices) {
                _selectedSession.value = sessions[nextIndex]
            }
        }
    }

    private fun applyTimeFilter(session: Session, filter: TimeFilter): Boolean {
        if (filter == TimeFilter.ALL) return true
        val local = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
        return when (filter) {
            TimeFilter.MORNING -> local.hour in 5..11
            TimeFilter.AFTERNOON -> local.hour in 12..17
            TimeFilter.EVENING -> local.hour in 18..23 || local.hour in 0..4
        }
    }

    private fun applyDateFilter(session: Session, selectedDate: Instant): Boolean {
        val sessionDate = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val filterDate = selectedDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return sessionDate == filterDate
    }

    private fun generateDummyData() {
        viewModelScope.launch {
            val dummySessions = PlaceholderData.getDummySessions()
            dummySessions.forEach { repository.saveSession(it) }
        }
    }
}
