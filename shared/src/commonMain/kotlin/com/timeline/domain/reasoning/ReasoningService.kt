package com.timeline.domain.reasoning

import com.timeline.domain.ml.ImageAnalysisResult
import kotlinx.coroutines.flow.Flow

interface ReasoningService {
    /**
     * Tier 1: Local reasoning using regex and heuristics.
     * Always fast, always private.
     */
    fun performLocalHeuristics(ocrTexts: List<String>): LocalEntities

    /**
     * Tier 2: Cloud-based reasoning using Gemini.
     * Consolidates a day's worth of app data into a narrative.
     */
    suspend fun generateDailyNarrative(
        packageName: String,
        appName: String,
        date: String,
        ocrDumps: List<String>,
        labels: List<String>,
        previousDaySummary: String? = null,
        mode: HighlightReasoningMode = HighlightReasoningMode.BALANCED
    ): Result<AppDailyReasoning>
}

class LocalHeuristicService : ReasoningService {
    override fun performLocalHeuristics(ocrTexts: List<String>): LocalEntities {
        val combined = ocrTexts.joinToString(" ")
        
        val emails = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            .findAll(combined).map { it.value }.distinct().toList()
            
        val urls = Regex("https?://[\\w\\d./%?=&-]+")
            .findAll(combined).map { it.value }.distinct().toList()
            
        val dates = Regex("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}")
            .findAll(combined).map { it.value }.distinct().toList()

        return LocalEntities(emails = emails, urls = urls, dates = dates)
    }

    override suspend fun generateDailyNarrative(
        packageName: String,
        appName: String,
        date: String,
        ocrDumps: List<String>,
        labels: List<String>,
        previousDaySummary: String?,
        mode: HighlightReasoningMode
    ): Result<AppDailyReasoning> {
        val sanitizedDumps = ocrDumps.map { PiiRedactor.redact(it) }
        val combined = sanitizedDumps.joinToString("\n")
        val lowerPkg = packageName.lowercase()

        // Infer cognitive mode from package name, labels, and text content
        val cognitiveMode = when {
            lowerPkg.contains("slack") || lowerPkg.contains("message") || lowerPkg.contains("telegram") || lowerPkg.contains("whatsapp") || lowerPkg.contains("mail") -> "Communication"
            lowerPkg.contains("code") || lowerPkg.contains("git") || lowerPkg.contains("docs") || lowerPkg.contains("studio") || lowerPkg.contains("sheet") -> "Deep Focus"
            lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("search") -> "Information Seeking"
            lowerPkg.contains("figma") || lowerPkg.contains("canvas") || lowerPkg.contains("photo") || lowerPkg.contains("design") -> "Creative"
            labels.any { it.equals("software", ignoreCase = true) || it.equals("computer", ignoreCase = true) } -> "Deep Focus"
            else -> "Productive Session"
        }

        // Extract action items from bullet points, checklists, or TODO markers
        val detectedTasks = mutableListOf<String>()
        combined.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*") || 
                trimmed.startsWith("TODO", ignoreCase = true) || trimmed.startsWith("Sprint", ignoreCase = true) ||
                trimmed.startsWith("Review", ignoreCase = true) || trimmed.startsWith("Implement", ignoreCase = true)) {
                val clean = trimmed.trimStart('•', '-', '*', ' ').trim()
                if (clean.length in 8..100) {
                    detectedTasks.add(clean)
                }
            }
        }

        val baseSummary = when (mode) {
            HighlightReasoningMode.CONCISE -> "Active usage session in $appName."
            HighlightReasoningMode.BALANCED -> when (cognitiveMode) {
                "Communication" -> "Coordinated and shared updates with team members in $appName."
                "Deep Focus" -> "Maintained high concentration on planning and documentation in $appName."
                "Information Seeking" -> "Researched resources in $appName."
                "Creative" -> "Explored visual workflows in $appName."
                else -> "Engaged in focused activity in $appName."
            }
            HighlightReasoningMode.EXPLANATORY -> when (cognitiveMode) {
                "Communication" -> "Focused on maintaining team alignment through proactive messaging and review of project threads within $appName."
                "Deep Focus" -> "Dedicated time to deep work, focusing on technical architecture, documentation, or implementation tasks within $appName."
                "Information Seeking" -> "Conducted comprehensive research, navigating documentation and online resources to support current work within $appName."
                "Creative" -> "Invested time in design iterations, exploring visual concepts and prototyping new ideas within $appName."
                else -> "Engaged in a productive, sustained work session, focusing on task execution and workflow management within $appName."
            }
        }

        val summary = if (!previousDaySummary.isNullOrBlank() && mode != HighlightReasoningMode.CONCISE) {
            "$baseSummary (Continuing progress from yesterday: ${previousDaySummary.take(40)}...)"
        } else {
            baseSummary
        }

        return Result.success(
            AppDailyReasoning(
                packageName = packageName,
                date = date,
                summary = summary,
                cognitiveMode = cognitiveMode,
                actionItems = if (mode == HighlightReasoningMode.CONCISE) emptyList() else detectedTasks.distinct().take(4)
            )
        )
    }
}
