package com.timeline.presentation

import com.timeline.domain.model.TrialStatus

data class MetricsState(
    val trialStatus: TrialStatus = TrialStatus.NOT_STARTED,
    val trialTimeRemaining: String = "",
    val dailyUsage: List<DailyUsage> = emptyList(),
    val appUsageStacked: List<StackedUsage> = emptyList(),
    val usageTrends: List<UsageTrend> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPeriod: MetricsPeriod = MetricsPeriod.WEEK,
    
    // Redesigned Overview Fields
    val usageChangePercentage: String = "0%",
    val isUsageIncreasing: Boolean = true,
    val biggestPatternTitle: String = "",
    val biggestPatternDescription: String = "",
    val biggestPatternPercentage: Int = 0,
    val topAppDisplayName: String = "",
    val topAppUsageTime: String = "",
    val topAppIcon: Any? = null,
    val mostActiveDayName: String = "",
    val mostActiveDayUsageTime: String = "",
    
    // Redesigned Apps Fields
    val appCount: Int = 0,
    val topApps: List<AppTimeShare> = emptyList(),
    val isAppsListExpanded: Boolean = false,
    val categoryInsightText: String = "",
    
    // Redesigned Trends Fields
    val usageMoreThanLastWeek: Boolean = true,
    
    // Redesigned Patterns Fields
    val peakActiveHour: Int = 0,
    val longestSessionAppName: String = "",
    val longestSessionTime: String = "",
    val heaviestDayName: String = "",
    val heaviestDayUsageTime: String = "",
    val lowestDayName: String = "",
    val lowestDayUsageTime: String = ""
)

enum class MetricsPeriod {
    DAY, WEEK, MONTH
}

data class DailyUsage(
    val day: String,
    val totalMinutes: Long,
    val percentageChange: Float,
    val isWeekend: Boolean = false
)

data class StackedUsage(
    val day: String,
    val appDistributions: List<AppTimeShare>
)

data class AppTimeShare(
    val packageName: String,
    val displayName: String?,
    val icon: Any?,
    val color: Int,
    val minutes: Long,
    val percentage: Float
)

data class UsageTrend(
    val timeLabel: String,
    val minutes: Long
)

sealed interface MetricsEvent {
    data object StartTrial : MetricsEvent
    data class ChangePeriod(val period: MetricsPeriod) : MetricsEvent
    data object ToggleAppsListExpansion : MetricsEvent
    data object Refresh : MetricsEvent
}

sealed interface MetricsEffect {
    data object TrialExpired : MetricsEffect
}
