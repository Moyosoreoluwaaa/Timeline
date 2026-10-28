package com.timeline.domain.reasoning

import com.timeline.domain.AppInfoProvider
import com.timeline.domain.Session
import com.timeline.presentation.TimeOfDayFilter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock

/**
 * Rich domain synthesizer that computes executive metrics, period breakdowns,
 * peak usage windows, cognitive modes, and natural-language narratives for Timeline highlights.
 */
object HighlightContentSynthesizer {

    enum class AppCategory(val title: String, val cognitiveMode: String) {
        DEVELOPMENT("Development & Engineering", "Deep Focus"),
        COMMUNICATION("Communication & Team", "Communication"),
        INFORMATION("Research & Information", "Information Seeking"),
        CREATIVE("Design & Creativity", "Creative"),
        ENTERTAINMENT("Media & Entertainment", "Casual"),
        PRODUCTIVITY("Productivity & Organization", "Deep Focus"),
        UTILITIES("System & Tools", "Administrative")
    }

    data class AppUsageStat(
        val packageName: String,
        val displayName: String,
        val totalMinutes: Long,
        val sessionCount: Int,
        val percentage: Float,
        val category: AppCategory
    )

    data class SynthesizedHighlight(
        val title: String,
        val narrative: String,
        val keyHighlights: List<String>,
        val cognitiveMode: String,
        val topApps: List<AppUsageStat>,
        val totalMinutes: Long,
        val sessionCount: Int,
        val peakWindow: String?,
        val longestSession: Session?,
        val actionItems: List<String>
    )

    fun inferCategory(packageName: String, displayName: String): AppCategory {
        val lower = (packageName + " " + displayName).lowercase()
        return when {
            lower.contains("slack") || lower.contains("message") || lower.contains("telegram") ||
                    lower.contains("whatsapp") || lower.contains("mail") || lower.contains("gmail") ||
                    lower.contains("discord") || lower.contains("outlook") || lower.contains("teams") ->
                AppCategory.COMMUNICATION

            lower.contains("code") || lower.contains("git") || lower.contains("studio") ||
                    lower.contains("terminal") || lower.contains("intellij") || lower.contains("compiler") ||
                    lower.contains("stackoverflow") || lower.contains("dev") ->
                AppCategory.DEVELOPMENT

            lower.contains("chrome") || lower.contains("browser") || lower.contains("search") ||
                    lower.contains("firefox") || lower.contains("wikipedia") || lower.contains("medium") ||
                    lower.contains("safari") || lower.contains("edge") ->
                AppCategory.INFORMATION

            lower.contains("figma") || lower.contains("canvas") || lower.contains("photo") ||
                    lower.contains("design") || lower.contains("sketch") || lower.contains("illustrat") ||
                    lower.contains("canva") || lower.contains("camera") || lower.contains("gallery") ->
                AppCategory.CREATIVE

            lower.contains("youtube") || lower.contains("netflix") || lower.contains("spotify") ||
                    lower.contains("music") || lower.contains("tiktok") || lower.contains("instagram") ||
                    lower.contains("twitter") || lower.contains("reddit") || lower.contains("game") ||
                    lower.contains("twitch") || lower.contains("disney") || lower.contains("prime") ->
                AppCategory.ENTERTAINMENT

            lower.contains("doc") || lower.contains("sheet") || lower.contains("slide") ||
                    lower.contains("notion") || lower.contains("note") || lower.contains("keep") ||
                    lower.contains("todo") || lower.contains("task") || lower.contains("trello") ||
                    lower.contains("asana") || lower.contains("calendar") || lower.contains("jira") ->
                AppCategory.PRODUCTIVITY

            else -> AppCategory.UTILITIES
        }
    }

    suspend fun synthesizeDayRecap(
        sessions: List<Session>,
        appInfoProvider: AppInfoProvider,
        tz: TimeZone = TimeZone.currentSystemDefault()
    ): SynthesizedHighlight {
        if (sessions.isEmpty()) {
            return SynthesizedHighlight(
                title = "Today's Recap",
                narrative = "No device activity recorded today. Your time was well spent offline.",
                keyHighlights = listOf("No screen activity recorded"),
                cognitiveMode = "Restful",
                topApps = emptyList(),
                totalMinutes = 0,
                sessionCount = 0,
                peakWindow = null,
                longestSession = null,
                actionItems = emptyList()
            )
        }

        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val sessionCount = sessions.size
        val longestSession = sessions.maxByOrNull { it.durationMinutes.coerceAtLeast(1) }

        // Group by app
        val appStats = sessions.groupBy { it.packageName }.map { (pkg, appSessions) ->
            val appMins = appSessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
            val name = appSessions.first().displayName ?: appInfoProvider.getAppName(pkg)
            val category = inferCategory(pkg, name)
            AppUsageStat(
                packageName = pkg,
                displayName = name,
                totalMinutes = appMins,
                sessionCount = appSessions.size,
                percentage = if (totalMinutes > 0) (appMins.toFloat() / totalMinutes) else 0f,
                category = category
            )
        }.sortedByDescending { it.totalMinutes }

        // Period breakdowns
        val morningMins = sessions.filter { it.startTime.toLocalDateTime(tz).hour in 6..11 }
            .sumOf { it.durationMinutes.coerceAtLeast(1) }
        val afternoonMins = sessions.filter { it.startTime.toLocalDateTime(tz).hour in 12..16 }
            .sumOf { it.durationMinutes.coerceAtLeast(1) }
        val eveningMins = sessions.filter {
            val h = it.startTime.toLocalDateTime(tz).hour
            h in 17..23 || h in 0..5
        }.sumOf { it.durationMinutes.coerceAtLeast(1) }

        val peakWindow = when {
            afternoonMins >= morningMins && afternoonMins >= eveningMins && afternoonMins > 0 ->
                "Afternoon (12 PM - 5 PM) with ${formatMinutes(afternoonMins)}"
            morningMins >= afternoonMins && morningMins >= eveningMins && morningMins > 0 ->
                "Morning (6 AM - 12 PM) with ${formatMinutes(morningMins)}"
            eveningMins > 0 ->
                "Evening (5 PM - 11 PM) with ${formatMinutes(eveningMins)}"
            else -> null
        }

        // Dominant cognitive mode
        val categoryMinutes = appStats.groupBy { it.category.cognitiveMode }
            .mapValues { (_, stats) -> stats.sumOf { it.totalMinutes } }
        val dominantModeEntry = categoryMinutes.maxByOrNull { it.value }
        val dominantCognitiveMode = dominantModeEntry?.key ?: "Balanced Productivity"
        val dominantPercentage = if (totalMinutes > 0 && dominantModeEntry != null) {
            (dominantModeEntry.value.toFloat() / totalMinutes * 100).roundToInt()
        } else 0

        // Compose end-of-day narrative
        val topAppsText = appStats.take(3).joinToString(", ") { "${it.displayName} (${formatMinutes(it.totalMinutes)})" }
        val durationFormatted = formatMinutes(totalMinutes)

        val rhythmSentence = when {
            afternoonMins > morningMins && afternoonMins > eveningMins ->
                "Peak activity unfolded in the afternoon, marked by sustained focus blocks."
            morningMins > afternoonMins && morningMins > eveningMins ->
                "Your screen time was front-loaded in the morning with proactive check-ins, tapering into a lighter afternoon."
            eveningMins > afternoonMins && eveningMins > morningMins ->
                "Activity peaked in the evening hours with relaxed media and wind-down workflows."
            else ->
                "Your activity was evenly distributed across the day with consistent check-ins."
        }

        val longestMins = longestSession?.durationMinutes?.coerceAtLeast(1) ?: 0
        val longestAppName = longestSession?.let { it.displayName ?: appInfoProvider.getAppName(it.packageName) }
        val outlierSentence = if (longestMins >= 20 && longestAppName != null) {
            "Your longest stretch was an uninterrupted $longestMins minutes in $longestAppName."
        } else {
            "$dominantCognitiveMode accounted for $dominantPercentage% of your total device time."
        }

        val narrative = "Today totaled $durationFormatted across ${appStats.size} apps, led by $topAppsText. $rhythmSentence $outlierSentence"

        val highlights = mutableListOf<String>()
        if (peakWindow != null) highlights.add("Peak focus window: $peakWindow")
        if (longestMins >= 15 && longestAppName != null) highlights.add("Longest session: $longestMins mins in $longestAppName")
        if (dominantPercentage > 0) highlights.add("$dominantCognitiveMode represented $dominantPercentage% of your day")

        val actionItems = extractSmartActionItems(sessions)

        return SynthesizedHighlight(
            title = "End of Day Recap",
            narrative = narrative,
            keyHighlights = highlights,
            cognitiveMode = dominantCognitiveMode,
            topApps = appStats,
            totalMinutes = totalMinutes,
            sessionCount = sessionCount,
            peakWindow = peakWindow,
            longestSession = longestSession,
            actionItems = actionItems
        )
    }

    suspend fun synthesizePeriodHighlight(
        filter: TimeOfDayFilter,
        sessions: List<Session>,
        appInfoProvider: AppInfoProvider,
        tz: TimeZone = TimeZone.currentSystemDefault()
    ): SynthesizedHighlight {
        val periodName = when (filter) {
            TimeOfDayFilter.MORNING -> "Morning"
            TimeOfDayFilter.AFTERNOON -> "Afternoon"
            TimeOfDayFilter.EVENING -> "Evening"
            TimeOfDayFilter.ALL -> "Today"
        }

        val currentHour = Clock.System.now().toLocalDateTime(tz).hour
        val isUpcoming = when (filter) {
            TimeOfDayFilter.MORNING -> currentHour < 6
            TimeOfDayFilter.AFTERNOON -> currentHour < 12
            TimeOfDayFilter.EVENING -> currentHour < 17
            TimeOfDayFilter.ALL -> false
        }

        if (sessions.isEmpty()) {
            val narrative = if (isUpcoming) {
                when (filter) {
                    TimeOfDayFilter.MORNING -> "Your morning recap arrives at 6 AM."
                    TimeOfDayFilter.AFTERNOON -> "Your afternoon recap arrives at 12 PM."
                    TimeOfDayFilter.EVENING -> "Your evening recap arrives at 5 PM."
                    TimeOfDayFilter.ALL -> "Your highlight recap will appear here once activity is recorded."
                }
            } else {
                when (filter) {
                    TimeOfDayFilter.MORNING -> "A quiet morning with minimal screen activity. Time was well spent offline."
                    TimeOfDayFilter.AFTERNOON -> "A quiet afternoon with minimal screen activity. Time was well spent offline."
                    TimeOfDayFilter.EVENING -> "A peaceful evening with little to no screen interaction."
                    TimeOfDayFilter.ALL -> "No screen activity recorded."
                }
            }

            return SynthesizedHighlight(
                title = "$periodName Digest",
                narrative = narrative,
                keyHighlights = emptyList(),
                cognitiveMode = if (isUpcoming) "Upcoming" else "Quiet",
                topApps = emptyList(),
                totalMinutes = 0,
                sessionCount = 0,
                peakWindow = null,
                longestSession = null,
                actionItems = emptyList()
            )
        }

        val totalMinutes = sessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
        val sessionCount = sessions.size
        val longestSession = sessions.maxByOrNull { it.durationMinutes.coerceAtLeast(1) }

        val appStats = sessions.groupBy { it.packageName }.map { (pkg, appSessions) ->
            val appMins = appSessions.sumOf { it.durationMinutes.coerceAtLeast(1) }
            val name = appSessions.first().displayName ?: appInfoProvider.getAppName(pkg)
            val category = inferCategory(pkg, name)
            AppUsageStat(
                packageName = pkg,
                displayName = name,
                totalMinutes = appMins,
                sessionCount = appSessions.size,
                percentage = (appMins.toFloat() / totalMinutes),
                category = category
            )
        }.sortedByDescending { it.totalMinutes }

        val dominantMode = appStats.firstOrNull()?.category?.cognitiveMode ?: "Deep Focus"
        val topAppNames = appStats.take(3).joinToString(" and ") { it.displayName }
        val topApp = appStats.first()
        val longestMins = longestSession?.durationMinutes?.coerceAtLeast(1) ?: 0
        val longestAppName = longestSession?.let { it.displayName ?: appInfoProvider.getAppName(it.packageName) }

        val narrative = when (filter) {
            TimeOfDayFilter.MORNING -> {
                if (sessionCount > 3) {
                    "${formatMinutes(totalMinutes)} this morning navigating $topAppNames. You switched between $sessionCount check-ins, starting the day with proactive momentum."
                } else {
                    "${formatMinutes(totalMinutes)} this morning, largely concentrated in ${topApp.displayName} (${formatMinutes(topApp.totalMinutes)}). A clear, uninterrupted start to your day."
                }
            }
            TimeOfDayFilter.AFTERNOON -> {
                if (longestMins >= 25 && longestAppName != null) {
                    "${formatMinutes(totalMinutes)} this afternoon centered in $topAppNames. Anchored by a $longestMins-minute continuous deep work stretch in $longestAppName."
                } else {
                    "${formatMinutes(totalMinutes)} this afternoon across $topAppNames ($sessionCount check-ins). Steady progress maintained without severe fragmentation."
                }
            }
            TimeOfDayFilter.EVENING -> {
                if (topApp.category == AppCategory.ENTERTAINMENT || topApp.category == AppCategory.COMMUNICATION) {
                    "${formatMinutes(totalMinutes)} this evening winding down with $topAppNames. A relaxed conclusion to your daily workflows."
                } else {
                    "${formatMinutes(totalMinutes)} this evening wrapping up tasks in $topAppNames. Final follow-ups before closing out the day."
                }
            }
            TimeOfDayFilter.ALL -> {
                "${formatMinutes(totalMinutes)} across $topAppNames today."
            }
        }

        val highlights = mutableListOf<String>()
        highlights.add("Top app: ${topApp.displayName} (${formatMinutes(topApp.totalMinutes)})")
        if (longestMins >= 20 && longestAppName != null) {
            highlights.add("Longest block: $longestMins mins in $longestAppName")
        }
        highlights.add("Primary focus: $dominantMode")

        return SynthesizedHighlight(
            title = "$periodName Digest",
            narrative = narrative,
            keyHighlights = highlights,
            cognitiveMode = dominantMode,
            topApps = appStats,
            totalMinutes = totalMinutes,
            sessionCount = sessionCount,
            peakWindow = null,
            longestSession = longestSession,
            actionItems = extractSmartActionItems(sessions)
        )
    }

    fun extractSmartActionItems(
        sessions: List<Session>,
        ocrDumps: List<String> = emptyList()
    ): List<String> {
        val detected = mutableListOf<String>()

        // 1. Check OCR text for explicit tasks
        ocrDumps.forEach { dump ->
            dump.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*") ||
                    trimmed.startsWith("TODO", ignoreCase = true) || trimmed.startsWith("Follow up", ignoreCase = true) ||
                    trimmed.startsWith("Review", ignoreCase = true) || trimmed.startsWith("Submit", ignoreCase = true)) {
                    val clean = trimmed.trimStart('•', '-', '*', ' ').trim()
                    if (clean.length in 8..80 && !detected.contains(clean)) {
                        detected.add(clean)
                    }
                }
            }
        }

        if (detected.isNotEmpty()) return detected.take(4)

        // 2. Intelligent suggestions based on active apps
        val uniqueCategories = sessions.map { inferCategory(it.packageName, it.displayName ?: "") }.distinct()
        uniqueCategories.forEach { category ->
            when (category) {
                AppCategory.COMMUNICATION -> {
                    val app = sessions.firstOrNull { inferCategory(it.packageName, it.displayName ?: "") == AppCategory.COMMUNICATION }
                    val name = app?.displayName ?: "messages"
                    detected.add("Review follow-up threads and unread mentions in $name")
                }
                AppCategory.DEVELOPMENT -> {
                    val app = sessions.firstOrNull { inferCategory(it.packageName, it.displayName ?: "") == AppCategory.DEVELOPMENT }
                    val name = app?.displayName ?: "development"
                    detected.add("Review active branch changes and commit daily progress in $name")
                }
                AppCategory.PRODUCTIVITY -> {
                    val app = sessions.firstOrNull { inferCategory(it.packageName, it.displayName ?: "") == AppCategory.PRODUCTIVITY }
                    val name = app?.displayName ?: "notes"
                    detected.add("Check off completed action items in $name")
                }
                AppCategory.INFORMATION -> {
                    val app = sessions.firstOrNull { inferCategory(it.packageName, it.displayName ?: "") == AppCategory.INFORMATION }
                    val name = app?.displayName ?: "browser"
                    detected.add("Archive or bookmark research tabs opened in $name")
                }
                else -> Unit
            }
        }

        return detected.take(3)
    }

    private fun formatMinutes(minutes: Long): String {
        return if (minutes < 60) {
            "${minutes}m"
        } else {
            val hours = minutes / 60
            val rem = minutes % 60
            if (rem == 0L) "${hours}h" else "${hours}h ${rem}m"
        }
    }
}
