package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private data class SelectionState(
    val selectedDate: Instant,
    val selectedPackageName: String?,
    val selectedSession: Session?,
    val fullScreenImagePath: String?
)

private data class ViewOptionsState(
    val isSheetExpanded: Boolean,
    val timeFilter: TimeFilter,
    val isLoading: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val exclusionPolicy: ExclusionPolicy
) : ViewModel() {

    private val _tutorialSessions = MutableStateFlow<List<Session>>(emptyList())
    val tutorialSessions: StateFlow<List<Session>> = _tutorialSessions.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    private val _selectedDate = MutableStateFlow(Clock.System.now())
    private val _selectedPackageName = MutableStateFlow<String?>(null)
    private val _selectedSession = MutableStateFlow<Session?>(null)
    private val _fullScreenImagePath = MutableStateFlow<String?>(null)
    private val _isSheetExpanded = MutableStateFlow(false)
    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    private val _isLoading = MutableStateFlow(true)

    // Flow for raw DB sessions driven by refresh triggers & managing loading state
    private val _dbSessions = _refreshTrigger
        .flatMapLatest { repository.getTimeline() }
        .onStart { _isLoading.value = true }
        .onEach { _isLoading.value = false }

    // Combine selection flows (4 flows)
    private val _selectionState = combine(
        _selectedDate,
        _selectedPackageName,
        _selectedSession,
        _fullScreenImagePath
    ) { date, pkg, session, image ->
        SelectionState(date, pkg, session, image)
    }

    // Combine view option flows (3 flows)
    private val _viewOptionsState = combine(
        _isSheetExpanded,
        _timeFilter,
        _isLoading
    ) { expanded, filter, loading ->
        ViewOptionsState(expanded, filter, loading)
    }

    // Primary StateFlow combining exactly 5 flows
    val state: StateFlow<TimelineState> = combine(
        _dbSessions,
        exclusionPolicy.getExcludedPackages(),
        _tutorialSessions,
        _selectionState,
        _viewOptionsState
    ) { sessionsFromDb, excluded, tutorialSessions, selection, viewOptions ->
        val sessions = tutorialSessions.ifEmpty { sessionsFromDb }
        var date = selection.selectedDate

        // Auto-select latest session date if selected date has no records
        val tz = TimeZone.currentSystemDefault()
        val filteredForDate = sessions.filter { applyDateFilter(it, date) && it.packageName !in excluded }
        if (filteredForDate.isEmpty() && sessions.isNotEmpty()) {
            val selectedLocalDate = date.toLocalDateTime(tz).date
            val todayLocalDate = Clock.System.now().toLocalDateTime(tz).date
            if (selectedLocalDate == todayLocalDate) {
                val latestSession = sessions.maxByOrNull { it.startTime }
                if (latestSession != null) {
                    date = latestSession.startTime
                    _selectedDate.value = date
                }
            }
        }

        val filteredSessions = filterSessions(
            sessions = sessions,
            excluded = excluded,
            date = date,
            filter = viewOptions.timeFilter,
            packageName = selection.selectedPackageName
        )
        val summary = calculateSummary(filteredSessions)
        val related = if (selection.selectedSession != null) {
            sessions.filter { it.packageName == selection.selectedSession.packageName && applyDateFilter(it, date) }
        } else emptyList()

        TimelineState(
            sessions = filteredSessions,
            summary = summary,
            isLoading = viewOptions.isLoading,
            selectedDate = date,
            selectedPackageName = selection.selectedPackageName,
            selectedSession = selection.selectedSession,
            relatedSessions = related,
            fullScreenImagePath = selection.fullScreenImagePath,
            isSheetExpanded = viewOptions.isSheetExpanded,
            timeFilter = viewOptions.timeFilter
        )
    }.mapLatest { s ->
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
        s.copy(
            sessions = enrichedSessions,
            relatedSessions = enrichedRelated,
            selectedSession = enrichedSelected,
            summary = enrichedSummary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineState(isLoading = true)
    )

    private val _effects = Channel<TimelineEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _realInteractions = MutableSharedFlow<TimelineEvent>(replay = 0, extraBufferCapacity = 4)
    val realInteractions: SharedFlow<TimelineEvent> = _realInteractions

    fun updateTutorialSessions(sessions: List<Session>) {
        _tutorialSessions.value = sessions
    }

    fun onEvent(event: TimelineEvent) {
        Logger.d { "TimelineViewModel onEvent: $event" }
        when (event) {
            is TimelineEvent.Refresh -> _refreshTrigger.value++
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
        _realInteractions.tryEmit(event)
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
}