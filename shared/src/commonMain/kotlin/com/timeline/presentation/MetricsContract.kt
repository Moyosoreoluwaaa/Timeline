package com.timeline.presentation

import com.timeline.domain.model.TrialStatus
import kotlin.time.Instant

data class MetricsState(
    val trialStatus: TrialStatus = TrialStatus.NOT_STARTED,
    val trialTimeRemaining: String = "",
    val dailyUsage: List<DailyUsage> = emptyList(),
    val appUsageStacked: List<StackedUsage> = emptyList(),
    val usageTrends: List<UsageTrend> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPeriod: MetricsPeriod = MetricsPeriod.WEEK
)

enum class MetricsPeriod {
    DAY, WEEK, MONTH
}

data class DailyUsage(
    val day: String,
    val totalMinutes: Long,
    val percentageChange: Float // Compared to prev period
)

data class StackedUsage(
    val day: String,
    val appDistributions: List<AppTimeShare>
)

data class AppTimeShare(
    val packageName: String,
    val displayName: String?,
    val color: Int, // Hex color for chart
    val minutes: Long
)

data class UsageTrend(
    val timeLabel: String, // e.g., "Morning", "Afternoon"
    val minutes: Long
)

sealed interface MetricsEvent {
    data object StartTrial : MetricsEvent
    data class ChangePeriod(val period: MetricsPeriod) : MetricsEvent
    data object Refresh : MetricsEvent
}

sealed interface MetricsEffect {
    data object TrialExpired : MetricsEffect
}
