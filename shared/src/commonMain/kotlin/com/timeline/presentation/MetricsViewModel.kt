package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.data.TimelineRepository
import com.timeline.domain.Session
import com.timeline.domain.UserPreferences
import com.timeline.domain.model.TrialStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class MetricsViewModel(
    private val repository: TimelineRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(MetricsState())
    val state = _state.asStateFlow()

    init {
        observePreferences()
        observeData()
        startTrialTimer()
    }

    private fun observePreferences() {
        userPreferences.state.onEach { prefs ->
            val status = calculateTrialStatus(prefs.trialStartedAt)
            val remaining = calculateRemainingTime(prefs.trialStartedAt)
            _state.update { it.copy(
                trialStatus = status,
                trialTimeRemaining = remaining
            ) }
        }.launchIn(viewModelScope)
    }

    private fun observeData() {
        combine(
            repository.getTimeline(),
            _state.map { it.selectedPeriod }.distinctUntilChanged()
        ) { sessions, _ ->
            val daily = aggregateDailyUsage(sessions)
            val stacked = aggregateStackedUsage(sessions)
            val trends = aggregateTrends(sessions)

            _state.update { it.copy(
                dailyUsage = daily,
                appUsageStacked = stacked,
                usageTrends = trends
            ) }
        }.launchIn(viewModelScope)
    }

    private fun startTrialTimer() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val prefs = userPreferences.state.first()
                val status = calculateTrialStatus(prefs.trialStartedAt)
                if (status == TrialStatus.EXPIRED && _state.value.trialStatus != TrialStatus.EXPIRED) {
                    _state.update { it.copy(trialStatus = TrialStatus.EXPIRED) }
                }
            }
        }
    }

    private fun calculateTrialStatus(startedAt: Long?): TrialStatus {
        if (startedAt == null) return TrialStatus.NOT_STARTED
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsed = now - startedAt
        val trialDurationMs = 15L * 60L * 1000L
        return if (elapsed > trialDurationMs) {
            TrialStatus.EXPIRED
        } else {
            TrialStatus.ACTIVE
        }
    }

    private fun calculateRemainingTime(startedAt: Long?): String {
        if (startedAt == null) return ""
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsed = now - startedAt
        val total = 15L * 60L * 1000L
        val remaining = total - elapsed
        if (remaining <= 0) return "0:00"
        val totalSeconds = remaining / 1000L
        val minutesPart = totalSeconds / 60L
        val secondsPart = totalSeconds % 60L
        return "${minutesPart}:${secondsPart.toString().padStart(2, '0')}"
    }

    private fun aggregateDailyUsage(sessions: List<Session>): List<DailyUsage> {
        return sessions.groupBy { 
            it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() 
        }.map { (date, daySessions) ->
            DailyUsage(
                day = date.takeLast(5),
                totalMinutes = daySessions.sumOf { it.durationMinutes },
                percentageChange = 0f
            )
        }.sortedBy { it.day }.takeLast(7)
    }

    private fun aggregateStackedUsage(sessions: List<Session>): List<StackedUsage> {
        return sessions.groupBy {
            it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        }.map { (date, daySessions) ->
            val distributions = daySessions.groupBy { it.packageName }.map { (pkg, pkgSessions) ->
                AppTimeShare(
                    packageName = pkg,
                    displayName = pkgSessions.firstOrNull()?.displayName,
                    color = getPackageColor(pkg),
                    minutes = pkgSessions.sumOf { it.durationMinutes }
                )
            }
            StackedUsage(date.takeLast(5), distributions)
        }.sortedBy { it.day }.takeLast(7)
    }

    private fun aggregateTrends(sessions: List<Session>): List<UsageTrend> {
        val morning = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 5..11 }
        val afternoon = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 12..17 }
        val evening = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 18..23 || it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 0..4 }

        return listOf(
            UsageTrend("Morning", morning.sumOf { it.durationMinutes }),
            UsageTrend("Afternoon", afternoon.sumOf { it.durationMinutes }),
            UsageTrend("Evening", evening.sumOf { it.durationMinutes })
        )
    }

    private fun getPackageColor(pkg: String): Int {
        val hash = pkg.hashCode()
        return (0xFF000000 or (hash and 0xFFFFFF).toLong()).toInt()
    }

    fun onEvent(event: MetricsEvent) {
        when (event) {
            MetricsEvent.StartTrial -> {
                viewModelScope.launch {
                    userPreferences.startTrial(Clock.System.now().toEpochMilliseconds())
                }
            }
            is MetricsEvent.ChangePeriod -> {
                _state.update { it.copy(selectedPeriod = event.period) }
            }
            MetricsEvent.Refresh -> {}
        }
    }
}
