package com.timeline.presentation

import com.timeline.domain.AppInfoProvider
import com.timeline.domain.Session
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.reasoning.HighlightContentSynthesizer
import kotlinx.datetime.TimeZone

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
        appInfoProvider: AppInfoProvider,
        tz: TimeZone = TimeZone.currentSystemDefault()
    ): HighlightSegment {
        val synthesized = HighlightContentSynthesizer.synthesizePeriodHighlight(
            filter = filter,
            sessions = sessions,
            appInfoProvider = appInfoProvider,
            tz = tz
        )

        val narrative = savedReasoning?.summary ?: synthesized.narrative
        val actionItems = if (!savedReasoning?.actionItems.isNullOrEmpty()) {
            savedReasoning.actionItems
        } else {
            synthesized.actionItems
        }

        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val durationFormatted = if (sessions.isEmpty()) {
            if (synthesized.cognitiveMode == "Upcoming") "Upcoming" else "Quiet"
        } else {
            formatDuration(totalMinutes)
        }

        val appNames = synthesized.topApps.map { it.displayName }
        val screenshots = sessions.flatMap { session ->
            session.screenshots + session.segments.mapNotNull { it.screenshotPath }
        }.distinct()

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

    fun formatDuration(minutes: Long): String {
        return when {
            minutes < 1 -> "0m" // Or "secs" if we had seconds here
            minutes < 60 -> "${minutes}m"
            else -> {
                val hours = minutes / 60
                val minRem = minutes % 60
                "${hours}h ${minRem}m"
            }
        }
    }
}
