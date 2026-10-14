package com.timeline.presentation

import com.timeline.domain.AppInfoProvider
import com.timeline.domain.Session
import com.timeline.domain.reasoning.AppDailyReasoning

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

        val narrative = savedReasoning?.summary ?: buildDefaultNarrative(filter, appNames)
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
        val emptyNarrative = when (filter) {
            TimeOfDayFilter.MORNING -> "A quiet morning with minimal device screen activity. Time was well spent offline."
            TimeOfDayFilter.AFTERNOON -> "No active digital workflows recorded during the afternoon hours."
            TimeOfDayFilter.EVENING -> "A peaceful evening with little to no screen interaction."
            else -> "No screen activity recorded for this period."
        }
        return HighlightSegment(
            filter = filter,
            title = title,
            timeRange = timeRange,
            narrative = emptyNarrative,
            apps = emptyList(),
            duration = "Inactive",
            screenshots = emptyList(),
            actionItems = emptyList()
        )
    }

    private fun buildDefaultNarrative(filter: TimeOfDayFilter, appNames: List<String>): String {
        val topAppsText = if (appNames.isNotEmpty()) appNames.take(4).joinToString(", ") else "various tools"
        val periodName = when (filter) {
            TimeOfDayFilter.MORNING -> "morning"
            TimeOfDayFilter.AFTERNOON -> "afternoon"
            TimeOfDayFilter.EVENING -> "evening"
            else -> "day"
        }
        return when (filter) {
            TimeOfDayFilter.MORNING -> "Started the $periodName with active engagements across $topAppsText. Focused on priority check-ins and kickstarting key tasks."
            TimeOfDayFilter.AFTERNOON -> "Peak productivity during the $periodName navigating $topAppsText. Accomplished workflow milestones and document reviews."
            TimeOfDayFilter.EVENING -> "Wind-down during the $periodName using $topAppsText. Wrapped up loose ends, caught up on updates, and prepared for tomorrow."
            else -> "Active usage recorded across $topAppsText."
        }
    }

    fun formatDuration(minutes: Long): String {
        return if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            if (m > 0) "${h}h ${m}m" else "${h}h"
        } else {
            "${minutes}m"
        }
    }
}
