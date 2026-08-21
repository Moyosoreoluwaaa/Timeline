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
import kotlin.random.Random
import kotlin.time.Clock as KClock
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val exclusionPolicy: ExclusionPolicy
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _selectedDate = MutableStateFlow<kotlinx.datetime.Instant?>(null)
    private val _selectedSession = MutableStateFlow<Session?>(null)
    private val _isSheetExpanded = MutableStateFlow(false)
    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<TimelineState> = combine(
        repository.getTimeline(),
        exclusionPolicy.getExcludedPackages(),
        _selectedDate,
        _selectedSession,
        _isSheetExpanded,
        _timeFilter,
        _isLoading
    ) { args: Array<Any?> ->
        val sessions = args[0] as List<Session>
        val excluded = args[1] as Set<String>
        val date = args[2] as kotlinx.datetime.Instant?
        val session = args[3] as Session?
        val expanded = args[4] as Boolean
        val filter = args[5] as TimeFilter
        val loading = args[6] as Boolean

        val filteredSessions = sessions
            .filter { it.packageName !in excluded }
            .filter { applyTimeFilter(it, filter) }

        TimelineState(
            sessions = filteredSessions,
            isLoading = loading,
            selectedDate = date,
            selectedSession = session,
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
            emit(s.copy(sessions = enrichedSessions))
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
            is TimelineEvent.SelectSession -> _selectedSession.value = event.session
            is TimelineEvent.ToggleSheet -> _isSheetExpanded.value = event.expanded
            is TimelineEvent.FilterTime -> _timeFilter.value = event.filter
            is TimelineEvent.SelectPreviousSession -> navigateSession(-1)
            is TimelineEvent.SelectNextSession -> navigateSession(1)
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
            else -> true
        }
    }

    private fun generateDummyData() {
        viewModelScope.launch {
            val apps = listOf("com.android.chrome", "com.google.android.youtube", "com.whatsapp", "com.instagram.android")
            val now = KClock.System.now()
            
            apps.forEachIndexed { index, pkg ->
                val session = Session(
                    id = "dummy_${index}_${Random.nextInt()}",
                    packageName = pkg,
                    startTime = now.minus((index * 30).minutes),
                    endTime = now.minus((index * 30 - 15).minutes),
                    durationMinutes = 15,
                    screenshots = listOf("/sdcard/dummy.png"),
                    segments = listOf(
                        com.timeline.domain.SessionSegment(
                            timestamp = now.minus((index * 30).minutes),
                            screenshotPath = "/sdcard/dummy.png",
                            activityDescription = "Application launched"
                        ),
                        com.timeline.domain.SessionSegment(
                            timestamp = now.minus((index * 30 - 5).minutes),
                            activityDescription = "User interacted with content"
                        )
                    )
                )
                repository.saveSession(session)
            }
            _refreshTrigger.value++
        }
    }
}
