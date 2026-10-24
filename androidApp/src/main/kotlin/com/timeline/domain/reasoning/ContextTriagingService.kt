package com.timeline.domain.reasoning

data class TriagedFrame(
    val sessionId: String,
    val packageName: String,
    val timestamp: Long,
    val text: String,
    val labels: List<String>,
    val confidenceScore: Float,
    val visualCategory: String?
)

data class TriagedSessionPayload(
    val packageName: String,
    val appName: String,
    val date: String,
    val frames: List<TriagedFrame>,
    val deduplicatedTextSamples: List<String>,
    val aggregatedLabels: List<String>,
    val previousDaySummary: String? = null
)

object ContextTriagingService {

    private const val MIN_CONFIDENCE_THRESHOLD = 0.35f
    private const val JACCARD_SIMILARITY_THRESHOLD = 0.85f

    private val boilerplateLines = setOf(
        "back", "home", "search", "share", "menu", "settings", "more options",
        "close", "cancel", "ok", "done", "next", "submit", "apply"
    )

    private val timeRegex = Regex("^\\d{1,2}:\\d{2}(?:\\s?[APap][Mm])?$")

    /**
     * Filters out low-confidence frames, deduplicates consecutive frames within the same session
     * using Jaccard token similarity (>85%), strips system UI boilerplate, and bundles
     * the payload with exact preceding day's context if available.
     */
    fun triageSession(
        packageName: String,
        appName: String,
        date: String,
        frames: List<TriagedFrame>,
        yesterdaySummary: String? = null
    ): TriagedSessionPayload {
        // 1. Filter out low-confidence frames (< 0.35)
        val validFrames = frames.filter { it.confidenceScore >= MIN_CONFIDENCE_THRESHOLD && it.text.isNotBlank() }

        // 2. Intra-session deduplication using Jaccard similarity (>85%)
        val deduplicatedFrames = mutableListOf<TriagedFrame>()
        for (frame in validFrames) {
            val lastFrame = deduplicatedFrames.lastOrNull()
            if (lastFrame != null && lastFrame.sessionId == frame.sessionId && lastFrame.packageName == frame.packageName) {
                val similarity = calculateJaccardSimilarity(lastFrame.text, frame.text)
                if (similarity >= JACCARD_SIMILARITY_THRESHOLD) {
                    // Frame is redundant, keep the longer or higher-confidence frame
                    if (frame.text.length > lastFrame.text.length || frame.confidenceScore > lastFrame.confidenceScore) {
                        deduplicatedFrames[deduplicatedFrames.lastIndex] = frame
                    }
                    continue
                }
            }
            deduplicatedFrames.add(frame)
        }

        // 3. Clean boilerplate from text samples
        val cleanedTextSamples = deduplicatedFrames.asSequence().map { frame ->
            cleanBoilerplate(frame.text)
        }.filter { it.isNotBlank() }.toList()

        // 4. Aggregate unique labels
        val allLabels = deduplicatedFrames.asSequence().flatMap { it.labels }.distinct().take(12).toList()

        // 5. Preceding day summary validation: only include if non-blank
        val validPreviousSummary = yesterdaySummary?.trim()?.ifBlank { null }

        return TriagedSessionPayload(
            packageName = packageName,
            appName = appName,
            date = date,
            frames = deduplicatedFrames,
            deduplicatedTextSamples = cleanedTextSamples,
            aggregatedLabels = allLabels,
            previousDaySummary = validPreviousSummary
        )
    }

    /**
     * Computes Jaccard similarity index between two texts based on unique word tokens.
     */
    fun calculateJaccardSimilarity(text1: String, text2: String): Float {
        val tokens1 = tokenize(text1)
        val tokens2 = tokenize(text2)

        if (tokens1.isEmpty() && tokens2.isEmpty()) return 1.0f
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0f

        val intersection = tokens1.intersect(tokens2).size
        val union = tokens1.union(tokens2).size

        return if (union == 0) 0.0f else intersection.toFloat() / union.toFloat()
    }

    /**
     * Cleans navigational boilerplate and standalone clock/status indicators while
     * preserving content lines.
     */
    fun cleanBoilerplate(text: String): String {
        return text.lines()
            .map { it.trim() }
            .filter { line ->
                if (line.isBlank()) return@filter false
                if (timeRegex.matches(line)) return@filter false
                val lower = line.lowercase()
                if (boilerplateLines.contains(lower)) return@filter false
                // Keep lines with meaningful length or content
                true
            }
            .joinToString("\n")
    }

    private fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .split(Regex("[^a-z0-9_#@]"))
            .filter { it.isNotBlank() && it.length > 2 }
            .toSet()
    }
}
