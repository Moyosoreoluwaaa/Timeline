package com.timeline.domain.reasoning

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

        val cleanAppName = appName.replace(Regex("""\(\s*\)"""), "").trim().ifBlank { "your apps" }
        val isDailySummary = cleanAppName.startsWith("Daily Summary", ignoreCase = true) || packageName.startsWith("ALL_APPS")

        // If a synthesized recap sentence is present in ocrDumps, prioritize it
        val providedRecap = sanitizedDumps.firstOrNull { it.contains("Today totaled", ignoreCase = true) || it.contains("across", ignoreCase = true) && it.contains("apps", ignoreCase = true) }

        val baseSummary = when {
            providedRecap != null -> providedRecap
            isDailySummary -> {
                when (mode) {
                    HighlightReasoningMode.CONCISE -> "Active daily usage across your core apps."
                    HighlightReasoningMode.BALANCED -> "A productive and well-balanced day navigating your key workflows."
                    HighlightReasoningMode.EXPLANATORY -> "A sustained and organized recap across your core daily workflows, maintaining steady progress on key tasks."
                }
            }
            else -> {
                val targetApp = cleanAppName
                when (mode) {
                    HighlightReasoningMode.CONCISE -> "Active usage session across $targetApp."
                    HighlightReasoningMode.BALANCED -> when (cognitiveMode) {
                        "Communication" -> "Coordinated and shared updates with team members in $cleanAppName."
                        "Deep Focus" -> "Maintained high concentration on planning and documentation in $cleanAppName."
                        "Information Seeking" -> "Researched resources in $cleanAppName."
                        "Creative" -> "Explored visual workflows in $cleanAppName."
                        else -> "Engaged in focused activity in $cleanAppName."
                    }
                    HighlightReasoningMode.EXPLANATORY -> when (cognitiveMode) {
                        "Communication" -> "Focused on maintaining team alignment through proactive messaging and review of project threads within $cleanAppName."
                        "Deep Focus" -> "Dedicated time to deep work, focusing on technical architecture, documentation, or implementation tasks within $cleanAppName."
                        "Information Seeking" -> "Conducted comprehensive research, navigating documentation and online resources to support current work within $cleanAppName."
                        "Creative" -> "Invested time in design iterations, exploring visual concepts and prototyping new ideas within $cleanAppName."
                        else -> "Engaged in a productive, sustained work session, focusing on task execution and workflow management within $cleanAppName."
                    }
                }
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
