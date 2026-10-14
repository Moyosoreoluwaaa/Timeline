package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.UserPreferences
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.MetricsAggregator.capitalizeName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class MetricsViewModel(
    private val repository: TimelineRepository,
    private val userPreferences: UserPreferences,
    private val appInfoProvider: AppInfoProvider
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
            val status = MetricsAggregator.calculateTrialStatus(prefs.trialStartedAt)
            val remaining = MetricsAggregator.calculateRemainingTime(prefs.trialStartedAt)
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
            val currentSessions = MetricsAggregator.filterSessionsByPeriod(sessions, period, 0)
            val previousSessions = MetricsAggregator.filterSessionsByPeriod(sessions, period, 1)

            val totalUsageCurrent = currentSessions.sumOf { it.durationMinutes }
            val totalUsagePrevious = previousSessions.sumOf { it.durationMinutes }

            val usageChange = if (totalUsagePrevious > 0) {
                ((totalUsageCurrent - totalUsagePrevious).toFloat() / totalUsagePrevious.toFloat() * 100).toInt()
            } else 0

            val isIncreasing = totalUsageCurrent >= totalUsagePrevious
            val daily = MetricsAggregator.aggregateDailyUsage(currentSessions, period)
            val stacked = MetricsAggregator.aggregateStackedUsage(currentSessions, period)
            val trends = MetricsAggregator.aggregateTrends(currentSessions, period)

            val mostUsedApp = currentSessions.groupBy { it.packageName }
                .maxByOrNull { it.value.sumOf { s -> s.durationMinutes } }

            val mostActiveDay = daily.maxByOrNull { it.totalMinutes }
            val leastActiveDay = daily.filter { it.totalMinutes > 0 }.minByOrNull { it.totalMinutes }
            val peakHour = MetricsAggregator.calculatePeakHour(currentSessions)

            val totalMinutes = currentSessions.sumOf { it.durationMinutes }
            val topAppsList = currentSessions.groupBy { it.packageName }.map { (pkg, pkgSessions) ->
                val pkgMinutes = pkgSessions.sumOf { it.durationMinutes }
                AppTimeShare(
                    packageName = pkg,
                    displayName = (pkgSessions.firstOrNull()?.displayName ?: pkg.split(".").last()).capitalizeName(),
                    icon = pkgSessions.firstOrNull()?.icon,
                    color = MetricsAggregator.getPackageColor(pkg),
                    minutes = pkgMinutes,
                    percentage = if (totalMinutes > 0) pkgMinutes.toFloat() / totalMinutes.toFloat() else 0f
                )
            }.sortedByDescending { it.minutes }

            val nightUsage = currentSessions.filter {
                it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour >= 19
            }.sumOf { it.durationMinutes }
            val nightPercentage = if (totalMinutes > 0) (nightUsage * 100 / totalMinutes).toInt() else 0

            val longestSession = currentSessions.maxByOrNull { it.durationMinutes }
            val categoryInsight = MetricsAggregator.calculateCategoryInsight(currentSessions)

            MetricsState(
                dailyUsage = daily,
                appUsageStacked = stacked,
                usageTrends = trends,
                usageChangePercentage = "${if (usageChange < 0) -usageChange else usageChange}%",
                isUsageIncreasing = isIncreasing,
                biggestPatternTitle = "Your biggest pattern",
                biggestPatternDescription = if (nightPercentage > 30) "You use your phone most after 7 PM." else "Your usage is evenly distributed.",
                biggestPatternPercentage = nightPercentage,
                topAppDisplayName = (mostUsedApp?.value?.firstOrNull()?.displayName ?: mostUsedApp?.key?.split(".")?.last() ?: "None").capitalizeName(),
                topAppUsageTime = "${(mostUsedApp?.value?.sumOf { it.durationMinutes } ?: 0) / 60}h ${(mostUsedApp?.value?.sumOf { it.durationMinutes } ?: 0) % 60}m",
                topAppIcon = mostUsedApp?.value?.firstOrNull()?.icon,
                mostActiveDayName = MetricsAggregator.getFullDayName(mostActiveDay?.day),
                mostActiveDayUsageTime = "${mostActiveDay?.totalMinutes?.div(60) ?: 0}h ${mostActiveDay?.totalMinutes?.rem(60) ?: 0}m",
                appCount = currentSessions.distinctBy { it.packageName }.size,
                topApps = topAppsList,
                categoryInsightText = categoryInsight,
                usageMoreThanLastWeek = isIncreasing,
                peakActiveHour = peakHour,
                longestSessionAppName = (longestSession?.displayName ?: longestSession?.packageName?.split(".")?.last() ?: "").capitalizeName(),
                longestSessionTime = "${longestSession?.durationMinutes ?: 0}m",
                heaviestDayName = MetricsAggregator.getFullDayName(mostActiveDay?.day),
                heaviestDayUsageTime = "${mostActiveDay?.totalMinutes?.div(60) ?: 0}h ${mostActiveDay?.totalMinutes?.rem(60) ?: 0}m",
                lowestDayName = MetricsAggregator.getFullDayName(leastActiveDay?.day),
                lowestDayUsageTime = "${leastActiveDay?.totalMinutes?.div(60) ?: 0}h ${leastActiveDay?.totalMinutes?.rem(60) ?: 0}m",
                selectedPeriod = period
            )
        }.flatMapLatest { s ->
            flow {
                val enrichedTopApps = s.topApps.map { app ->
                    app.copy(
                        displayName = appInfoProvider.getAppName(app.packageName).capitalizeName(),
                        icon = appInfoProvider.getAppIcon(app.packageName)
                    )
                }

                val topAppPkg = s.topApps.firstOrNull()?.packageName
                val enrichedTopAppName = topAppPkg?.let { appInfoProvider.getAppName(it).capitalizeName() } ?: s.topAppDisplayName
                val enrichedTopAppIcon = topAppPkg?.let { appInfoProvider.getAppIcon(it) } ?: s.topAppIcon

                emit(s.copy(
                    topApps = enrichedTopApps,
                    topAppDisplayName = enrichedTopAppName,
                    topAppIcon = enrichedTopAppIcon
                ))
            }
        }.onEach { newState ->
            _state.update { newState.copy(
                trialStatus = it.trialStatus,
                trialTimeRemaining = it.trialTimeRemaining,
                isAppsListExpanded = it.isAppsListExpanded
            ) }
        }.launchIn(viewModelScope)
    }

    private fun startTrialTimer() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val prefs = userPreferences.state.first()
                val status = MetricsAggregator.calculateTrialStatus(prefs.trialStartedAt)
                if (status == TrialStatus.EXPIRED && _state.value.trialStatus != TrialStatus.EXPIRED) {
                    _state.update { it.copy(trialStatus = TrialStatus.EXPIRED) }
                }
            }
        }
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
