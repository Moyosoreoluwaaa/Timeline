package com.timeline.presentation

import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.ml.HighlightAnalysisCache
import com.timeline.domain.reasoning.LocalHeuristicService
import com.timeline.domain.ml.VisionAnalysisService
import com.timeline.domain.reasoning.ContextTriagingService
import com.timeline.domain.reasoning.HighlightReasoningMode
import com.timeline.domain.reasoning.PiiRedactor
import com.timeline.domain.reasoning.PrivacyExclusionProvider
import com.timeline.domain.reasoning.ReasoningService
import com.timeline.domain.reasoning.TriagedFrame
import kotlinx.coroutines.flow.firstOrNull

/**
 * Coordinates multi-stage reasoning, OCR extraction, local PII redaction, and narrative synthesis.
 */
class RawHighlightReasoningCoordinator(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val visionService: VisionAnalysisService,
    private val reasoningService: ReasoningService,
    private val localHeuristicService: LocalHeuristicService,
    private val logger: Logger
) {

    suspend fun runDailyReasoningPipeline(
        dateString: String,
        yesterdayDateString: String,
        mode: HighlightReasoningMode,
        allItems: List<RawHighlightScreenshotItem>,
        onProgress: (Float) -> Unit,
        onStage: (RawHighlightState.ReasoningStage) -> Unit,
        onShieldActive: () -> Unit,
        onAppReasoningGenerated: (packageName: String, reasoning: com.timeline.domain.reasoning.AppDailyReasoning, icon: String) -> Unit,
        onOverallReasoningGenerated: (reasoning: com.timeline.domain.reasoning.AppDailyReasoning) -> Unit
    ) {
        val groupedByApp = allItems.groupBy { it.packageName }
        val allTriagedFrames = mutableListOf<TriagedFrame>()
        val groupedList = groupedByApp.toList()

        groupedList.forEachIndexed { index, (packageName, items) ->
            onProgress((index.toFloat() / groupedList.size.toFloat()) * 0.5f)
            if (PrivacyExclusionProvider.isPackageExcluded(packageName)) return@forEachIndexed

            val analyses = items.mapNotNull { item ->
                val cached = repository.getAnalysisResult(item.screenshotPath)
                if (cached != null) {
                    HighlightAnalysisCache.put(item.screenshotPath, cached)
                    cached
                } else {
                    visionService.analyzeImageFromPath(item.screenshotPath).getOrNull()?.also { analysis ->
                        repository.saveAnalysisResult(item.screenshotPath, analysis)
                        HighlightAnalysisCache.put(item.screenshotPath, analysis)
                    }
                }
            }

            val appName = items.firstOrNull()?.displayName ?: appInfoProvider.getAppName(packageName)

            onStage(RawHighlightState.ReasoningStage.REDACTING)
            val triagedFrames = items.zip(analyses).map { (item, analysis) ->
                val originalText = analysis.textResult.fullText
                val redactedText = PiiRedactor.redact(originalText)
                if (redactedText != originalText) {
                    onShieldActive()
                }
                TriagedFrame(
                    sessionId = item.sessionId,
                    packageName = packageName,
                    timestamp = item.timestamp.toEpochMilliseconds(),
                    text = redactedText,
                    labels = analysis.labels.map { it.text },
                    confidenceScore = analysis.confidenceScore,
                    visualCategory = analysis.visualCategory
                )
            }
            allTriagedFrames.addAll(triagedFrames)

            onStage(RawHighlightState.ReasoningStage.CONTEXTUALIZING)
            val yesterdaySummary = repository.getAppDailyReasoning(packageName, yesterdayDateString).firstOrNull()?.summary

            onStage(RawHighlightState.ReasoningStage.DEDUPLICATING)
            val triaged = ContextTriagingService.triageSession(
                packageName = packageName,
                appName = appName,
                date = dateString,
                frames = triagedFrames,
                yesterdaySummary = yesterdaySummary
            )

            val validDumps = if (triaged.deduplicatedTextSamples.isNotEmpty()) {
                triaged.deduplicatedTextSamples
            } else {
                listOf("Active usage session in $appName")
            }

            onStage(RawHighlightState.ReasoningStage.REASONING)
            val result = reasoningService.generateDailyNarrative(
                packageName = packageName,
                appName = appName,
                date = dateString,
                ocrDumps = validDumps,
                labels = triaged.aggregatedLabels,
                previousDaySummary = triaged.previousDaySummary,
                mode = mode
            )

            result.onSuccess { reasoning ->
                repository.saveAppDailyReasoning(reasoning)
                onAppReasoningGenerated(packageName, reasoning, getIconForMode(reasoning.cognitiveMode))
            }.onFailure { err ->
                logger.w(err) { "Reasoning failed for $packageName, applying local fallback" }
                val fallbackReasoning = localHeuristicService.generateDailyNarrative(
                    packageName = packageName,
                    appName = appName,
                    date = dateString,
                    ocrDumps = validDumps,
                    labels = triaged.aggregatedLabels,
                    previousDaySummary = triaged.previousDaySummary,
                    mode = mode
                ).getOrNull()
                if (fallbackReasoning != null) {
                    repository.saveAppDailyReasoning(fallbackReasoning)
                    onAppReasoningGenerated(packageName, fallbackReasoning, getIconForMode(fallbackReasoning.cognitiveMode))
                }
            }
        }

        // Daily executive narrative for all apps combined
        if (groupedByApp.isNotEmpty()) {
            val appNames = groupedByApp.keys.map { pkg ->
                allItems.firstOrNull { it.packageName == pkg }?.displayName ?: appInfoProvider.getAppName(pkg)
            }.distinct().joinToString(", ")
            val summaryAppName = if (appNames.isNotBlank()) "Daily Summary ($appNames)" else "Daily Summary"

            val yesterdayOverallSummary = repository.getAppDailyReasoning("ALL_APPS", yesterdayDateString).firstOrNull()?.summary

            val triagedAll = ContextTriagingService.triageSession(
                packageName = "ALL_APPS",
                appName = summaryAppName,
                date = dateString,
                frames = allTriagedFrames,
                yesterdaySummary = yesterdayOverallSummary
            )

            val promptDumps = if (triagedAll.deduplicatedTextSamples.isNotEmpty()) {
                triagedAll.deduplicatedTextSamples.take(8)
            } else {
                listOf("Active real device sessions recorded today across: $appNames")
            }

            val overallResult = reasoningService.generateDailyNarrative(
                packageName = "ALL_APPS",
                appName = summaryAppName,
                date = dateString,
                ocrDumps = promptDumps,
                labels = triagedAll.aggregatedLabels,
                previousDaySummary = triagedAll.previousDaySummary,
                mode = mode
            )
            overallResult.onSuccess { overallReasoning ->
                repository.saveAppDailyReasoning(overallReasoning)
                onOverallReasoningGenerated(overallReasoning)
            }.onFailure {
                val fallback = localHeuristicService.generateDailyNarrative(
                    packageName = "ALL_APPS",
                    appName = summaryAppName,
                    date = dateString,
                    ocrDumps = promptDumps,
                    labels = triagedAll.aggregatedLabels,
                    previousDaySummary = triagedAll.previousDaySummary,
                    mode = mode
                ).getOrNull()
                if (fallback != null) {
                    repository.saveAppDailyReasoning(fallback)
                    onOverallReasoningGenerated(fallback)
                }
            }
        }
    }

    fun getIconForMode(mode: String): String {
        return when (mode.lowercase()) {
            "deep focus" -> "brain"
            "communication" -> "chat"
            "social" -> "people"
            "productivity" -> "terminal"
            "entertainment" -> "play"
            else -> "info"
        }
    }
}
