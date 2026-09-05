package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.Session
import com.timeline.domain.UserPreferences
import com.timeline.domain.model.TrialStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
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
            val currentSessions = filterSessionsByPeriod(sessions, period, 0)
            val previousSessions = filterSessionsByPeriod(sessions, period, 1)
            
            val totalUsageCurrent = currentSessions.sumOf { it.durationMinutes }
            val totalUsagePrevious = previousSessions.sumOf { it.durationMinutes }
            
            val usageChange = if (totalUsagePrevious > 0) {
                ((totalUsageCurrent - totalUsagePrevious).toFloat() / totalUsagePrevious.toFloat() * 100).toInt()
            } else 0
            
            val isIncreasing = totalUsageCurrent >= totalUsagePrevious
            
            val daily = aggregateDailyUsage(currentSessions, period)
            val stacked = aggregateStackedUsage(currentSessions, period)
            val trends = aggregateTrends(currentSessions, period)
            
            val mostUsedApp = currentSessions.groupBy { it.packageName }
                .maxByOrNull { it.value.sumOf { s -> s.durationMinutes } }
            
            val mostActiveDay = daily.maxByOrNull { it.totalMinutes }
            val leastActiveDay = daily.filter { it.totalMinutes > 0 }.minByOrNull { it.totalMinutes }
            
            val peakHour = calculatePeakHour(currentSessions)
            
            val totalMinutes = currentSessions.sumOf { it.durationMinutes }
            val topAppsList = currentSessions.groupBy { it.packageName }.map { (pkg, pkgSessions) ->
                val pkgMinutes = pkgSessions.sumOf { it.durationMinutes }
                AppTimeShare(
                    packageName = pkg,
                    displayName = (pkgSessions.firstOrNull()?.displayName ?: pkg.split(".").last()).capitalizeName(),
                    icon = pkgSessions.firstOrNull()?.icon,
                    color = getPackageColor(pkg),
                    minutes = pkgMinutes,
                    percentage = if (totalMinutes > 0) pkgMinutes.toFloat() / totalMinutes.toFloat() else 0f
                )
            }.sortedByDescending { it.minutes }

            val nightUsage = currentSessions.filter { 
                it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour >= 19 
            }.sumOf { it.durationMinutes }
            val nightPercentage = if (totalMinutes > 0) (nightUsage * 100 / totalMinutes).toInt() else 0
            
            val longestSession = currentSessions.maxByOrNull { it.durationMinutes }
            
            val categoryInsight = calculateCategoryInsight(currentSessions)

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
                mostActiveDayName = getFullDayName(mostActiveDay?.day),
                mostActiveDayUsageTime = "${mostActiveDay?.totalMinutes?.div(60) ?: 0}h ${mostActiveDay?.totalMinutes?.rem(60) ?: 0}m",
                appCount = currentSessions.distinctBy { it.packageName }.size,
                topApps = topAppsList,
                categoryInsightText = categoryInsight,
                usageMoreThanLastWeek = isIncreasing,
                peakActiveHour = peakHour,
                longestSessionAppName = (longestSession?.displayName ?: longestSession?.packageName?.split(".")?.last() ?: "").capitalizeName(),
                longestSessionTime = "${longestSession?.durationMinutes ?: 0}m",
                heaviestDayName = getFullDayName(mostActiveDay?.day),
                heaviestDayUsageTime = "${mostActiveDay?.totalMinutes?.div(60) ?: 0}h ${mostActiveDay?.totalMinutes?.rem(60) ?: 0}m",
                lowestDayName = getFullDayName(leastActiveDay?.day),
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

    private fun filterSessionsByPeriod(sessions: List<Session>, period: MetricsPeriod, offset: Int): List<Session> {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(zone).date
        
        return when (period) {
            MetricsPeriod.DAY -> {
                val targetDate = today.minus(offset, DateTimeUnit.DAY)
                val start = targetDate.atStartOfDayIn(zone)
                val end = targetDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone)
                sessions.filter { it.startTime >= start && it.startTime < end }
            }
            MetricsPeriod.WEEK -> {
                val start = now.minus((offset + 1) * 7, DateTimeUnit.DAY, zone)
                val end = now.minus(offset * 7, DateTimeUnit.DAY, zone)
                sessions.filter { it.startTime >= start && it.startTime < end }
            }
            MetricsPeriod.MONTH -> {
                val start = now.minus((offset + 1) * 30, DateTimeUnit.DAY, zone)
                val end = now.minus(offset * 30, DateTimeUnit.DAY, zone)
                sessions.filter { it.startTime >= start && it.startTime < end }
            }
        }
    }

    private fun calculatePeakHour(sessions: List<Session>): Int {
        val hourlyUsage = IntArray(24)
        sessions.forEach { session ->
            val hour = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour
            hourlyUsage[hour] += session.durationMinutes.toInt()
        }
        return hourlyUsage.indices.maxByOrNull { hourlyUsage[it] } ?: 0
    }

    private fun calculateCategoryInsight(sessions: List<Session>): String {
        val categoryUsage = mutableMapOf<String, Long>()
        sessions.forEach { session ->
            val cat = getCategoryForPackage(session.packageName)
            categoryUsage[cat] = (categoryUsage[cat] ?: 0L) + session.durationMinutes
        }
        val topCat = categoryUsage.maxByOrNull { it.value } ?: return "apps this period"
        val hours = topCat.value / 60
        val mins = topCat.value % 60
        return "${hours}h ${mins}m on ${topCat.key.lowercase()} apps this period"
    }

    private fun getCategoryForPackage(pkg: String): String {
        return when {
            pkg.contains("video") || pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("disney") || pkg.contains("player") -> "Video"
            pkg.contains("social") || pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("x.android") || pkg.contains("tiktok") || pkg.contains("whatsapp") -> "Social"
            pkg.contains("game") || pkg.contains("play") -> "Games"
            pkg.contains("browser") || pkg.contains("chrome") || pkg.contains("firefox") -> "Browsing"
            pkg.contains("work") || pkg.contains("office") || pkg.contains("mail") || pkg.contains("slack") -> "Productivity"
            else -> "Other"
        }
    }

    private fun getFullDayName(abbreviation: String?): String {
        return when (abbreviation) {
            "M" -> "Monday"
            "Tu" -> "Tuesday"
            "W" -> "Wednesday"
            "Th" -> "Thursday"
            "F" -> "Friday"
            "Sa" -> "Saturday"
            "Su" -> "Sunday"
            "Today" -> "Today"
            else -> "N/A"
        }
    }

    private fun getUniqueAbbreviation(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> "M"
            DayOfWeek.TUESDAY -> "Tu"
            DayOfWeek.WEDNESDAY -> "W"
            DayOfWeek.THURSDAY -> "Th"
            DayOfWeek.FRIDAY -> "F"
            DayOfWeek.SATURDAY -> "Sa"
            DayOfWeek.SUNDAY -> "Su"
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
                day = if (period == MetricsPeriod.DAY) "Today" else getUniqueAbbreviation(date.dayOfWeek),
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
            StackedUsage(if (period == MetricsPeriod.DAY) "Today" else getUniqueAbbreviation(date.dayOfWeek), distributions)
        }
    }

    private fun aggregateTrends(sessions: List<Session>, period: MetricsPeriod): List<UsageTrend> {
        return when (period) {
            MetricsPeriod.DAY -> {
                // Hourly trends for Day
                (0..23).map { hour ->
                    val hourUsage = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour == hour }
                        .sumOf { it.durationMinutes }
                    UsageTrend(hour.toString(), hourUsage)
                }
            }
            else -> {
                // Daily trends for Week/Month
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val dayCount = if (period == MetricsPeriod.WEEK) 7 else 30
                (0 until dayCount).reversed().map { daysAgo ->
                    val date = today.minus(daysAgo, DateTimeUnit.DAY)
                    val dayUsage = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date == date }
                        .sumOf { it.durationMinutes }
                    UsageTrend(getUniqueAbbreviation(date.dayOfWeek), dayUsage)
                }
            }
        }
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
