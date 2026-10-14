package com.timeline.presentation

import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.ml.HighlightAnalysisCache
import com.timeline.domain.ml.ImageAnalysisResult
import com.timeline.domain.ml.VisionAnalysisService
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.reasoning.LocalEntities
import com.timeline.domain.reasoning.LocalHeuristicService
import kotlinx.coroutines.flow.firstOrNull

internal data class AnalysisExecutionResult(
    val analysis: ImageAnalysisResult?,
    val localEntities: LocalEntities?,
    val error: String?
)

internal object RawHighlightAnalysisExecutor {

    suspend fun execute(
        item: RawHighlightScreenshotItem,
        visionService: VisionAnalysisService,
        repository: TimelineRepository,
        localHeuristicService: LocalHeuristicService,
        logger: Logger
    ): AnalysisExecutionResult {
        val cached = HighlightAnalysisCache.get(item.screenshotPath)
        if (cached != null) {
            val entities = localHeuristicService.performLocalHeuristics(listOf(cached.textResult.fullText))
            return AnalysisExecutionResult(cached, entities, null)
        }

        return try {
            val result = visionService.analyzeImageFromPath(item.screenshotPath)
            result.fold(
                onSuccess = { analysis ->
                    repository.saveAnalysisResult(item.screenshotPath, analysis)
                    HighlightAnalysisCache.put(item.screenshotPath, analysis)
                    val entities = localHeuristicService.performLocalHeuristics(listOf(analysis.textResult.fullText))
                    AnalysisExecutionResult(analysis, entities, null)
                },
                onFailure = { err ->
                    AnalysisExecutionResult(null, null, err.message)
                }
            )
        } catch (e: Exception) {
            logger.e(e) { "Error analyzing image: ${item.screenshotPath}" }
            AnalysisExecutionResult(null, null, e.message)
        }
    }

    suspend fun loadExistingReasonings(
        items: List<RawHighlightScreenshotItem>,
        dateString: String,
        repository: TimelineRepository
    ): Map<String, AppDailyReasoning> {
        val uniquePackages = (items.map { it.packageName } + "ALL_APPS").distinct()
        val results = mutableMapOf<String, AppDailyReasoning>()
        uniquePackages.forEach { pkg ->
            repository.getAppDailyReasoning(pkg, dateString).firstOrNull()?.let { reasoning ->
                results[pkg] = reasoning
            }
        }
        return results
    }
}
