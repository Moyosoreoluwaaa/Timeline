package com.timeline.data.repository

import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.Session
import com.timeline.domain.model.ReelSegment
import com.timeline.domain.model.ReelSlide
import com.timeline.domain.model.ReelSlideKind
import com.timeline.domain.model.ReelSlideScreenshot
import com.timeline.domain.repository.ReelHighlightRepository
import com.timeline.presentation.TimeOfDayFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Production implementation of [ReelHighlightRepository].
 *
 * Maps real Timeline sessions (Room DB) and AI reasoning (AppDailyReasoning) into
 * the [ReelSegment] / [ReelSlide] format consumed by [ReelHighlightViewModel] and
 * the story-reel UI.
 *
 * Color palette (consistent with prototype):
 *   Teal   bg=0xFF5CD6C4  fg=0xFF06302B  ac=0xFFB8F0E8
 *   Amber  bg=0xFFFFB938  fg=0xFF2B1D00  ac=0xFFFFE3A6
 *   Indigo bg=0xFFA9B0FF  fg=0xFF1F2250  ac=0xFFD6D9FF
 *   Pink   bg=0xFFF27DC3  fg=0xFF3A0E2E  ac=0xFFFFC2E6
 */
class TimelineReelRepositoryImpl(
    private val timelineRepository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val deviceUsageSyncer: DeviceUsageSyncer
) : ReelHighlightRepository {

    // ---------- Palette ----------
    private val colorTealBg = 0xFF5CD6C4L
    private val colorTealFg = 0xFF06302BL
    private val colorTealAc = 0xFFB8F0E8L

    private val colorAmberBg = 0xFFFFB938L
    private val colorAmberFg = 0xFF2B1D00L
    private val colorAmberAc = 0xFFFFE3A6L

    private val colorIndBg = 0xFFA9B0FFL
    private val colorIndFg = 0xFF1F2250L
    private val colorIndAc = 0xFFD6D9FFL

    private val colorPinkBg = 0xFFF27DC3L
    private val colorPinkFg = 0xFF3A0E2EL
    private val colorPinkAc = 0xFFFFC2E6L

    // ---------- Public API ----------

    override fun getDaySegments(): Flow<List<ReelSegment>> = flow {
        timelineRepository.getTimeline().collect { sessions ->
            val tz = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(tz).date
            val todaySessions = sessions.filter {
                it.startTime.toLocalDateTime(tz).date == today
            }
            val dateString = today.toString()
            val reasoning = timelineRepository
                .getAppDailyReasoning("ALL_APPS", dateString)
                .firstOrNull()

            emit(
                listOf(
                    buildSegment(
                        filter = TimeOfDayFilter.MORNING,
                        title = "Morning",
                        sessions = filterByPeriod(todaySessions, TimeOfDayFilter.MORNING, tz),
                        reasoning = reasoning,
                        bgColor = colorAmberBg
                    ),
                    buildSegment(
                        filter = TimeOfDayFilter.AFTERNOON,
                        title = "Afternoon",
                        sessions = filterByPeriod(todaySessions, TimeOfDayFilter.AFTERNOON, tz),
                        reasoning = reasoning,
                        bgColor = colorTealBg
                    ),
                    buildSegment(
                        filter = TimeOfDayFilter.EVENING,
                        title = "Evening",
                        sessions = filterByPeriod(todaySessions, TimeOfDayFilter.EVENING, tz),
                        reasoning = reasoning,
                        bgColor = colorIndBg
                    )
                )
            )
        }
    }

    override suspend fun getReelSlides(filter: TimeOfDayFilter): List<ReelSlide> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val sessions = timelineRepository.getTimeline().firstOrNull() ?: emptyList()
        val todaySessions = sessions.filter {
            it.startTime.toLocalDateTime(tz).date == today
        }
        val periodSessions = filterByPeriod(todaySessions, filter, tz)
        val reasoning = timelineRepository
            .getAppDailyReasoning("ALL_APPS", today.toString())
            .firstOrNull()
        return buildSlidesForPeriod(filter, periodSessions, reasoning, tz)
    }

    override suspend fun syncRealDeviceUsage() {
        deviceUsageSyncer.syncRealDeviceUsage()
    }

    // ---------- Private helpers ----------

    private fun filterByPeriod(
        sessions: List<Session>,
        filter: TimeOfDayFilter,
        tz: TimeZone
    ): List<Session> = sessions.filter { session ->
        val hour = session.startTime.toLocalDateTime(tz).hour
        when (filter) {
            TimeOfDayFilter.MORNING -> hour in 6..11
            TimeOfDayFilter.AFTERNOON -> hour in 12..16
            TimeOfDayFilter.EVENING -> hour >= 17 || hour < 6
            TimeOfDayFilter.ALL -> true
        }
    }

    private suspend fun buildSegment(
        filter: TimeOfDayFilter,
        title: String,
        sessions: List<Session>,
        reasoning: com.timeline.domain.reasoning.AppDailyReasoning?,
        bgColor: Long
    ): ReelSegment {
        val currentHour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).hour

        if (sessions.isEmpty()) {
            val isUpcoming = when (filter) {
                TimeOfDayFilter.MORNING -> currentHour < 6
                TimeOfDayFilter.AFTERNOON -> currentHour < 12
                TimeOfDayFilter.EVENING -> currentHour < 17
                TimeOfDayFilter.ALL -> false
            }
            val upcomingNarrative = when (filter) {
                TimeOfDayFilter.MORNING -> "Your morning recap arrives at 6 AM."
                TimeOfDayFilter.AFTERNOON -> "Your afternoon recap arrives at 12 PM."
                TimeOfDayFilter.EVENING -> "Your evening recap arrives at 5 PM."
                TimeOfDayFilter.ALL -> "Your recap will appear here once activity is recorded."
            }
            val quietNarrative = when (filter) {
                TimeOfDayFilter.MORNING -> "A quiet morning with minimal screen activity."
                TimeOfDayFilter.AFTERNOON -> "A quiet afternoon with minimal screen activity."
                TimeOfDayFilter.EVENING -> "A quiet evening with minimal screen activity."
                TimeOfDayFilter.ALL -> "No activity recorded."
            }
            return ReelSegment(
                filter = filter,
                title = title,
                narrative = if (isUpcoming) upcomingNarrative else quietNarrative,
                paragraphs = listOf(if (isUpcoming) upcomingNarrative else quietNarrative),
                accentColor = bgColor,
                isUpcoming = isUpcoming
            )
        }

        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val appNames = sessions
            .map { it.displayName ?: appInfoProvider.getAppName(it.packageName) }
            .distinct()
        val narrative =
            reasoning?.summary ?: buildNarrative(filter, appNames, totalMinutes, sessions.size)

        return ReelSegment(
            filter = filter,
            title = title,
            narrative = narrative,
            paragraphs = listOf(narrative),
            accentColor = bgColor,
            isUpcoming = false
        )
    }

    private suspend fun buildSlidesForPeriod(
        filter: TimeOfDayFilter,
        sessions: List<Session>,
        reasoning: com.timeline.domain.reasoning.AppDailyReasoning?,
        tz: TimeZone
    ): List<ReelSlide> {
        val slides = mutableListOf<ReelSlide>()

        val (bg, fg, ac) = when (filter) {
            TimeOfDayFilter.MORNING -> Triple(colorAmberBg, colorAmberFg, colorAmberAc)
            TimeOfDayFilter.AFTERNOON -> Triple(colorTealBg, colorTealFg, colorTealAc)
            TimeOfDayFilter.EVENING -> Triple(colorIndBg, colorIndFg, colorIndAc)
            TimeOfDayFilter.ALL -> Triple(colorTealBg, colorTealFg, colorTealAc)
        }

        if (sessions.isEmpty()) {
            // Show a placeholder stat slide when no sessions
            val periodName = when (filter) {
                TimeOfDayFilter.MORNING -> "Morning"
                TimeOfDayFilter.AFTERNOON -> "Afternoon"
                TimeOfDayFilter.EVENING -> "Evening"
                TimeOfDayFilter.ALL -> "Today"
            }
            slides.add(
                ReelSlide(
                    id = "empty_${filter.name}",
                    kind = ReelSlideKind.STAT,
                    keyLabel = periodName,
                    headline = "No screen activity recorded in this period.",
                    stat = "0m",
                    sub = "screen time",
                    bgColorHex = bg,
                    fgColorHex = fg,
                    accentColorHex = ac
                )
            )
            return slides
        }

        // Build a RECAP slide per unique app (up to 3), using real session timestamps
        val appGroups = sessions
            .groupBy { it.packageName }
            .entries
            .take(3)

        appGroups.forEachIndexed { index, (packageName, appSessions) ->
            val appName = appSessions.first().displayName
                ?: appInfoProvider.getAppName(packageName)
            val firstSession = appSessions.minByOrNull { it.startTime }
            val lastSession = appSessions.maxByOrNull { it.startTime }
            val totalAppMins = appSessions.sumOf { it.durationMinutes.coerceAtLeast(1) }

            val shots = appSessions.take(2).mapIndexed { shotIndex, session ->
                val localTime = session.startTime.toLocalDateTime(tz)
                val timeLabel = "%d:%02d %s".format(
                    if (localTime.hour % 12 == 0) 12 else localTime.hour % 12,
                    localTime.minute,
                    if (localTime.hour < 12) "AM" else "PM"
                )
                ReelSlideScreenshot(
                    appName = appName,
                    iconKey = packageName,
                    colorHex = appColorForPackage(packageName),
                    timestamp = timeLabel,
                    isHighlighted = shotIndex == appSessions.size - 1
                )
            }

            // Alternate slide colors for visual interest
            val slidePalette = when (index % 3) {
                0 -> Triple(bg, fg, ac)
                1 -> Triple(colorAmberBg, colorAmberFg, colorAmberAc)
                else -> Triple(colorIndBg, colorIndFg, colorIndAc)
            }

            val narrativeHeadline = reasoning?.summary?.let {
                // Use first sentence of reasoning summary for first slide
                if (index == 0) it.split(".").firstOrNull()?.trim()?.let { s -> "$s." }
                else null
            } ?: buildSlideHeadline(appName, totalAppMins, appGroups.size)

            slides.add(
                ReelSlide(
                    id = "${filter.name.lowercase()}_recap_$index",
                    kind = ReelSlideKind.RECAP,
                    keyLabel = "",
                    headline = narrativeHeadline,
                    shots = shots,
                    bgColorHex = slidePalette.first,
                    fgColorHex = slidePalette.second,
                    accentColorHex = slidePalette.third
                )
            )
        }

        // Final stat slide
        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val statFormatted = if (totalMinutes < 60) "${totalMinutes}m"
        else "${totalMinutes / 60}h ${totalMinutes % 60}m"

        val periodLabel = when (filter) {
            TimeOfDayFilter.MORNING -> "Morning total"
            TimeOfDayFilter.AFTERNOON -> "Afternoon total"
            TimeOfDayFilter.EVENING -> "Evening total"
            TimeOfDayFilter.ALL -> "Today's total"
        }

        slides.add(
            ReelSlide(
                id = "${filter.name.lowercase()}_stat",
                kind = ReelSlideKind.STAT,
                keyLabel = periodLabel,
                headline = buildStatHeadline(sessions.size, totalMinutes),
                stat = statFormatted,
                sub = "screen time",
                bgColorHex = colorPinkBg,
                fgColorHex = colorPinkFg,
                accentColorHex = colorPinkAc
            )
        )

        return slides
    }

    private fun buildSlideHeadline(appName: String, minutes: Long, totalApps: Int): String {
        return when {
            minutes >= 30 -> "$appName occupied most of this period — $minutes minutes without a break."
            totalApps == 1 -> "All $minutes minutes went to $appName."
            else -> "$appName for $minutes minutes."
        }
    }

    private fun buildStatHeadline(sessionCount: Int, totalMinutes: Long): String {
        val sessionsText = if (sessionCount == 1) "one session" else "$sessionCount sessions"
        return when {
            totalMinutes < 10 -> "A brief check-in — $sessionsText, $totalMinutes minutes."
            totalMinutes < 30 -> "$sessionsText, $totalMinutes minutes."
            else -> "$sessionsText totalling $totalMinutes minutes."
        }
    }

    private fun buildNarrative(
        filter: TimeOfDayFilter,
        appNames: List<String>,
        totalMinutes: Long,
        sessionCount: Int
    ): String {
        val topApps = appNames.take(3).joinToString(" and ")
        return when (filter) {
            TimeOfDayFilter.MORNING ->
                "$totalMinutes minutes this morning across $topApps — $sessionCount focused check-ins."

            TimeOfDayFilter.AFTERNOON ->
                "$totalMinutes minutes this afternoon in $topApps."

            TimeOfDayFilter.EVENING ->
                "$totalMinutes minutes this evening winding down with $topApps."

            TimeOfDayFilter.ALL ->
                "$totalMinutes minutes across $topApps."
        }
    }

    /** Deterministic accent color seeded by package name for consistent visual identity. */
    private fun appColorForPackage(packageName: String): Long {
        val knownColors = mapOf(
            "com.google.android.youtube" to 0xFFE5442FL,
            "com.amazon.mShop.android.shopping" to 0xFFFF9900L,
            "com.android.chrome" to 0xFF4285F4L,
            "com.google.android.apps.maps" to 0xFF34A853L,
            "com.google.android.apps.messaging" to 0xFF25A45FL,
            "com.spotify.music" to 0xFF1DB954L,
            "com.google.android.gm" to 0xFFEA4335L
        )
        return knownColors[packageName]
            ?: (0xFF000000L or (packageName.hashCode().toLong().and(0xFFFFFFL)))
    }
}
