package com.timeline.presentation

import com.timeline.domain.Session
import com.timeline.domain.model.TrialStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Pure aggregation and statistics calculator for device usage sessions.
 */
internal object MetricsAggregator {

    fun filterSessionsByPeriod(sessions: List<Session>, period: MetricsPeriod, offset: Int): List<Session> {
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

    fun calculatePeakHour(sessions: List<Session>): Int {
        val hourlyUsage = IntArray(24)
        sessions.forEach { session ->
            val hour = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour
            hourlyUsage[hour] += session.durationMinutes.toInt()
        }
        return hourlyUsage.indices.maxByOrNull { hourlyUsage[it] } ?: 0
    }

    fun calculateCategoryInsight(sessions: List<Session>): String {
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

    fun getCategoryForPackage(pkg: String): String {
        return when {
            pkg.contains("video") || pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("disney") || pkg.contains("player") -> "Video"
            pkg.contains("social") || pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("x.android") || pkg.contains("tiktok") || pkg.contains("whatsapp") -> "Social"
            pkg.contains("game") || pkg.contains("play") -> "Games"
            pkg.contains("browser") || pkg.contains("chrome") || pkg.contains("firefox") -> "Browsing"
            pkg.contains("work") || pkg.contains("office") || pkg.contains("mail") || pkg.contains("slack") -> "Productivity"
            else -> "Other"
        }
    }

    fun getFullDayName(abbreviation: String?): String {
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

    fun getUniqueAbbreviation(day: DayOfWeek): String {
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

    fun String.capitalizeName(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    fun calculateTrialStatus(startedAt: Long?): TrialStatus {
        if (startedAt == null) return TrialStatus.NOT_STARTED
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsed = now - startedAt
        val trialDurationMs = 4L * 60L * 60L * 1000L
        return if (elapsed > trialDurationMs) TrialStatus.EXPIRED else TrialStatus.ACTIVE
    }

    fun calculateRemainingTime(startedAt: Long?): String {
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
        if (hoursPart > 0) return "${hoursPart}h ${minutesPart}m"
        return "${minutesPart}:${secondsPart.toString().padStart(2, '0')}"
    }

    fun aggregateDailyUsage(sessions: List<Session>, period: MetricsPeriod): List<DailyUsage> {
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

    fun aggregateStackedUsage(sessions: List<Session>, period: MetricsPeriod): List<StackedUsage> {
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

    fun aggregateTrends(sessions: List<Session>, period: MetricsPeriod): List<UsageTrend> {
        return when (period) {
            MetricsPeriod.DAY -> {
                (0..23).map { hour ->
                    val hourUsage = sessions.filter { it.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour == hour }
                        .sumOf { it.durationMinutes }
                    UsageTrend(hour.toString(), hourUsage)
                }
            }
            else -> {
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

    fun getPackageColor(pkg: String): Int {
        val hash = pkg.hashCode()
        return (0xFF000000 or (hash and 0xFFFFFF).toLong()).toInt()
    }
}
