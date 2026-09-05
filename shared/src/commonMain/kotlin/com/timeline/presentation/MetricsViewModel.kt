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
import kotlinx.datetime.*
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
        ) { sessions, period ->
            val filteredSessions = filterSessionsByPeriod(sessions, period)
            val daily = aggregateDailyUsage(filteredSessions, period)
            val stacked = aggregateStackedUsage(filteredSessions, period)
            val trends = aggregateTrends(filteredSessions)
            
            val totalUsageCurrent = filteredSessions.sumOf { it.durationMinutes }
            val usageChange = 12 // Simulated for now
            val isIncreasing = true
            
            val mostUsedApp = filteredSessions.groupBy { it.packageName }
                .maxByOrNull { it.value.sumOf { s -> s.durationMinutes } }
            
            val mostActiveDay = daily.maxByOrNull { it.totalMinutes }
            
            val nightUsage = filteredSessions.filter { 
                it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour >= 19 
            }.sumOf { it.durationMinutes }
            val nightPercentage = if (totalUsageCurrent > 0) (nightUsage * 100 / totalUsageCurrent).toInt() else 0
            
            val longestSession = filteredSessions.maxByOrNull { it.durationMinutes }
            
            val topAppsList = filteredSessions.groupBy { it.packageName }.map { (pkg, pkgSessions) ->
                val pkgMinutes = pkgSessions.sumOf { it.durationMinutes }
                AppTimeShare(
                    packageName = pkg,
                    displayName = (pkgSessions.firstOrNull()?.displayName ?: pkg.split(".").last()).capitalizeName(),
                    icon = pkgSessions.firstOrNull()?.icon,
                    color = getPackageColor(pkg),
                    minutes = pkgMinutes,
                    percentage = if (totalUsageCurrent > 0) pkgMinutes.toFloat() / totalUsageCurrent.toFloat() else 0f
                )
            }.sortedByDescending { it.minutes }

            _state.update { it.copy(
                dailyUsage = daily,
                appUsageStacked = stacked,
                usageTrends = trends,
                usageChangePercentage = "$usageChange%",
                isUsageIncreasing = isIncreasing,
                biggestPatternTitle = "Your biggest pattern",
                biggestPatternDescription = "You use your phone most after 7 PM.",
                biggestPatternPercentage = nightPercentage,
                topAppDisplayName = (mostUsedApp?.value?.firstOrNull()?.displayName ?: mostUsedApp?.key?.split(".")?.last() ?: "").capitalizeName(),
                topAppUsageTime = "${(mostUsedApp?.value?.sumOf { it.durationMinutes } ?: 0) / 60}h ${(mostUsedApp?.value?.sumOf { it.durationMinutes } ?: 0) % 60}m",
                topAppIcon = mostUsedApp?.value?.firstOrNull()?.icon,
                mostActiveDayName = mostActiveDay?.day ?: "",
                mostActiveDayUsageTime = "${mostActiveDay?.totalMinutes?.div(60)}h ${mostActiveDay?.totalMinutes?.rem(60)}m",
                appCount = filteredSessions.distinctBy { it.packageName }.size,
                topApps = topAppsList,
                categoryInsightText = "video apps this period",
                usageMoreThanLastWeek = isIncreasing,
                peakActiveHour = 9, // Simulated
                longestSessionAppName = (longestSession?.displayName ?: longestSession?.packageName?.split(".")?.last() ?: "").capitalizeName(),
                longestSessionTime = "${longestSession?.durationMinutes}m",
                heaviestDayName = mostActiveDay?.day ?: "",
                heaviestDayUsageTime = "${mostActiveDay?.totalMinutes?.div(60)}h ${mostActiveDay?.totalMinutes?.rem(60)}m"
            ) }
        }.launchIn(viewModelScope)
    }

    private fun filterSessionsByPeriod(sessions: List<Session>, period: MetricsPeriod): List<Session> {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        return when (period) {
            MetricsPeriod.DAY -> {
                val startOfDay = now.toLocalDateTime(zone).date.atStartOfDayIn(zone)
                sessions.filter { it.startTime >= startOfDay }
            }
            MetricsPeriod.WEEK -> {
                val startOfWeek = now.minus(7, DateTimeUnit.DAY, zone)
                sessions.filter { it.startTime >= startOfWeek }
            }
            MetricsPeriod.MONTH -> {
                val startOfMonth = now.minus(30, DateTimeUnit.DAY, zone)
                sessions.filter { it.startTime >= startOfMonth }
            }
        }
    }

    private fun String.capitalizeName(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

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
        val trialDurationMs = 4L * 60L * 60L * 1000L // 4 hours
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
        val total = 4L * 60L * 60L * 1000L
        val remaining = total - elapsed
        if (remaining <= 0) return "0:00"
        val totalSeconds = remaining / 1000L
        val hoursPart = totalSeconds / 3600L
        val minutesPart = (totalSeconds % 3600L) / 60L
        val secondsPart = totalSeconds % 60L
        if (hoursPart > 0) {
            return "${hoursPart}h ${minutesPart}m"
        }
        return "${minutesPart}:${secondsPart.toString().padStart(2, '0')}"
    }

    private fun aggregateDailyUsage(sessions: List<Session>, period: MetricsPeriod): List<DailyUsage> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dayCount = when (period) {
            MetricsPeriod.DAY -> 1
            MetricsPeriod.WEEK -> 7
            MetricsPeriod.MONTH -> 30
        }
        return (0 until dayCount).reversed().map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val daySessions = sessions.filter { 
                it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date == date 
            }
            DailyUsage(
                day = if (period == MetricsPeriod.DAY) "Today" else date.dayOfWeek.name.take(1),
                totalMinutes = daySessions.sumOf { it.durationMinutes },
                percentageChange = 0f,
                isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            )
        }
    }

    private fun aggregateStackedUsage(sessions: List<Session>, period: MetricsPeriod): List<StackedUsage> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dayCount = when (period) {
            MetricsPeriod.DAY -> 1
            MetricsPeriod.WEEK -> 7
            MetricsPeriod.MONTH -> 30
        }
        return (0 until dayCount).reversed().map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val daySessions = sessions.filter { 
                it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date == date 
            }
            val totalMinutes = daySessions.sumOf { it.durationMinutes }
            val distributions = daySessions.groupBy { it.packageName }.map { (pkg, pkgSessions) ->
                val pkgMinutes = pkgSessions.sumOf { it.durationMinutes }
                AppTimeShare(
                    packageName = pkg,
                    displayName = (pkgSessions.firstOrNull()?.displayName ?: pkg.split(".").last()).capitalizeName(),
                    icon = pkgSessions.firstOrNull()?.icon,
                    color = getPackageColor(pkg),
                    minutes = pkgMinutes,
                    percentage = if (totalMinutes > 0) pkgMinutes.toFloat() / totalMinutes.toFloat() else 0f
                )
            }
            StackedUsage(if (period == MetricsPeriod.DAY) "Today" else date.dayOfWeek.name.take(1), distributions)
        }
    }

    private fun aggregateTrends(sessions: List<Session>): List<UsageTrend> {
        val morning = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 5..11 }
        val afternoon = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 12..17 }
        val evening = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 18..23 || it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour in 0..4 }

        return listOf(
            UsageTrend("M", morning.sumOf { it.durationMinutes }),
            UsageTrend("A", afternoon.sumOf { it.durationMinutes }),
            UsageTrend("E", evening.sumOf { it.durationMinutes })
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
            MetricsEvent.ToggleAppsListExpansion -> {
                _state.update { it.copy(isAppsListExpanded = !it.isAppsListExpanded) }
            }
            MetricsEvent.Refresh -> {}
        }
    }
}
