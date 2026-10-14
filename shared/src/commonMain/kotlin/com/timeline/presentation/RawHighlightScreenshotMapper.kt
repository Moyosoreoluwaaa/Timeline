package com.timeline.presentation

import com.timeline.domain.AppInfoProvider
import com.timeline.domain.Session
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Pure mapping functions to convert domain sessions into RawHighlightScreenshotItem collections.
 */
object RawHighlightScreenshotMapper {

    fun findEffectiveDate(
        sessions: List<Session>,
        selectedDate: Instant?,
        tz: TimeZone = TimeZone.currentSystemDefault()
    ): Pair<Instant, LocalDate> {
        val date = selectedDate ?: Clock.System.now()
        val localDate = date.toLocalDateTime(tz).date
        val today = Clock.System.now().toLocalDateTime(tz).date
        val hasSessions = sessions.any { it.startTime.toLocalDateTime(tz).date == localDate }
        if (!hasSessions && sessions.isNotEmpty() && localDate == today) {
            val latest = sessions.maxByOrNull { it.startTime }
            if (latest != null) return latest.startTime to latest.startTime.toLocalDateTime(tz).date
        }
        return date to localDate
    }

    suspend fun mapSessionsToScreenshotItems(
        sessions: List<Session>,
        selectedLocalDate: LocalDate,
        appInfoProvider: AppInfoProvider,
        tz: TimeZone = TimeZone.currentSystemDefault()
    ): List<RawHighlightScreenshotItem> {
        val filteredSessions = sessions.filter {
            it.startTime.toLocalDateTime(tz).date == selectedLocalDate
        }

        return filteredSessions.flatMap { session ->
            val displayName = session.displayName ?: appInfoProvider.getAppName(session.packageName)
            val icon = session.icon ?: appInfoProvider.getAppIcon(session.packageName)
            val paths = (session.screenshots + session.segments.mapNotNull { it.screenshotPath }).distinct()

            paths.mapIndexed { index, path ->
                RawHighlightScreenshotItem(
                    id = "${session.id}_$index",
                    sessionId = session.id,
                    packageName = session.packageName,
                    displayName = displayName,
                    icon = icon,
                    screenshotPath = path,
                    timestamp = session.startTime,
                    durationMinutes = session.durationMinutes
                )
            }
        }
    }
}
