package com.timeline.presentation

import com.timeline.domain.AppInfoProvider
import com.timeline.domain.Session
import com.timeline.domain.reasoning.AppDailyReasoning
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Builds structured time-of-day highlight segments from sessions and AI reasoning.
 */
object HighlightSegmentBuilder {

    suspend fun buildSegment(
        filter: TimeOfDayFilter,
        title: String,
        timeRange: String,
        sessions: List<Session>,
        savedReasoning: AppDailyReasoning?,
        appInfoProvider: AppInfoProvider
    ): HighlightSegment {
        if (sessions.isEmpty()) {
            return buildEmptySegment(filter, title, timeRange)
        }

        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val durationFormatted = formatDuration(totalMinutes)

        val appNames = sessions
            .map { it.displayName ?: appInfoProvider.getAppName(it.packageName) }
            .distinct()

        val screenshots = sessions.flatMap { session ->
            session.screenshots + session.segments.mapNotNull { it.screenshotPath }
        }.distinct()

        val narrative = savedReasoning?.summary ?: buildDefaultNarrative(filter, appNames, totalMinutes, sessions.size)
        val actionItems = savedReasoning?.actionItems ?: emptyList()

        return HighlightSegment(
            filter = filter,
            title = title,
            timeRange = timeRange,
            narrative = narrative,
            apps = appNames,
            duration = durationFormatted,
            screenshots = screenshots,
            actionItems = actionItems
        )
    }

    private fun buildEmptySegment(
        filter: TimeOfDayFilter,
        title: String,
        timeRange: String
    ): HighlightSegment {
        val now = Clock.System.now()
        val currentHour = now.toLocalDateTime(TimeZone.currentSystemDefault()).hour

        val (statusPill, emptyNarrative) = when (filter) {
            TimeOfDayFilter.MORNING -> {
                when {
                    currentHour < 6 -> Pair("Starts 6 AM", "Your morning recap arrives at 6 AM.")
                    currentHour in 6..11 -> Pair("Now", "A quiet morning so far with minimal device screen activity.")
                    else -> Pair("Quiet", "A quiet morning with minimal device screen activity. Time was well spent offline.")
                }
            }
            TimeOfDayFilter.AFTERNOON -> {
                when {
                    currentHour < 12 -> Pair("Starts 12 PM", "Your afternoon recap arrives at 12 PM.")
                    currentHour in 12..16 -> Pair("Now", "A quiet afternoon so far with minimal device screen activity.")
                    else -> Pair("Quiet", "A quiet afternoon with minimal device screen activity. Time was well spent offline.")
                }
            }
            TimeOfDayFilter.EVENING -> {
                when {
                    currentHour < 17 -> Pair("Starts 5 PM", "Your evening recap arrives at 5 PM.")
                    currentHour in 17..23 -> Pair("Now", "A peaceful evening so far with little to no screen interaction.")
                    else -> Pair("Quiet", "A peaceful evening with little to no screen interaction.")
                }
            }
            else -> Pair("Starts 12 PM", "Your highlight recap will appear here once activity is recorded.")
        }

        return HighlightSegment(
            filter = filter,
            title = title,
            timeRange = timeRange,
            narrative = emptyNarrative,
            apps = emptyList(),
            duration = statusPill,
            screenshots = emptyList(),
            actionItems = emptyList()
        )
    }

    private fun buildDefaultNarrative(
        filter: TimeOfDayFilter,
        appNames: List<String>,
        totalMinutes: Long,
        sessionCount: Int
    ): String {
        val topAppsText = if (appNames.isNotEmpty()) appNames.take(3).joinToString(" and ") else "your apps"
        val periodName = when (filter) {
            TimeOfDayFilter.MORNING -> "this morning"
            TimeOfDayFilter.AFTERNOON -> "this afternoon"
            TimeOfDayFilter.EVENING -> "this evening"
            else -> "today"
        }

        // 3-Part formula: specific numbers, plain-language observation, and one short takeaway
        return when (filter) {
            TimeOfDayFilter.MORNING -> {
                if (sessionCount > 3) {
                    "$totalMinutes minutes $periodName across $topAppsText. You switched apps $sessionCount times kickstarting key tasks. You stayed with it longer than usual."
                } else {
                    "$totalMinutes minutes $periodName, mostly in $topAppsText: focused check-ins, typing, sending. A solid, uninterrupted start to your day."
                }
            }
            TimeOfDayFilter.AFTERNOON -> {
                if (sessionCount > 4) {
                    "$totalMinutes minutes $periodName navigating $topAppsText. You hopped between workflows $sessionCount times. Milestones reached without losing momentum."
                } else {
                    "$totalMinutes minutes $periodName focused in $topAppsText. Sustained attention on priority documents and review."
                }
            }
            TimeOfDayFilter.EVENING -> {
                "$totalMinutes minutes $periodName winding down with $topAppsText. A relaxed session wrapping up loose ends before tomorrow."
            }
            else -> "$totalMinutes minutes recorded across $topAppsText."
        }
    }

    fun formatDuration(minutes: Long): String {
        return "${minutes}m"
    }
}
