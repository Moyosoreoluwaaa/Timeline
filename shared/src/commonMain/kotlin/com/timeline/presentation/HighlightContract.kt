package com.timeline.presentation

import com.timeline.domain.ml.ImageAnalysisResult
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.reasoning.LocalEntities
import kotlin.time.Instant

data class HighlightScreenshotItem(
    val id: String,
    val sessionId: String,
    val packageName: String,
    val displayName: String?,
    val icon: Any?,
    val screenshotPath: String,
    val timestamp: Instant,
    val durationMinutes: Long = 0
)

data class HighlightState(
    val screenshots: List<HighlightScreenshotItem> = emptyList(),
    val selectedScreenshot: HighlightScreenshotItem? = null,
    val selectedPackage: String? = null, // null means show all
    val selectedDate: Instant? = null,
    val isAnalyzing: Boolean = false,
    val isRefreshing: Boolean = false,
    val analyzingPaths: Set<String> = emptySet(),
    val analysisCache: Map<String, ImageAnalysisResult> = emptyMap(),
    val localEntitiesCache: Map<String, LocalEntities> = emptyMap(),
    val currentResult: ImageAnalysisResult? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val appDailyReasonings: Map<String, AppDailyReasoning> = emptyMap(), // key: packageName
    val isReasoningLoading: Boolean = false,
    val isAiOptedIn: Boolean = false,
    val isPro: Boolean = false,
    val isUsagePermissionGranted: Boolean = true,
    val isSyncingDeviceUsage: Boolean = false,
    val reasoningStage: ReasoningStage = ReasoningStage.IDLE,
    val isPrivacyShieldActive: Boolean = false,
    val categoryIcon: String? = null
) {

enum class ReasoningStage {
    IDLE, REDACTING, DEDUPLICATING, CONTEXTUALIZING, REASONING
}
    val filteredScreenshots: List<HighlightScreenshotItem>
        get() {
            var list = screenshots
            if (selectedPackage != null) {
                list = list.filter { it.packageName == selectedPackage }
            }
            // Date filter logic would go here if we want to filter the whole list,
            // but the user wants a date picker to "change the extracted content".
            // If they mean filter the screenshots by date, let's add it.
            return list
        }

    val uniqueAppItems: List<HighlightScreenshotItem>
        get() = screenshots.distinctBy { it.packageName }

    val availablePackages: Set<String>
        get() = screenshots.map { it.packageName }.toSet()

    val filteredTextBlocks: List<String>
        get() {
            val result = currentResult ?: return emptyList()
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                return result.textResult.blocks.map { it.text }
            }
            return result.textResult.blocks
                .map { it.text }
                .filter { it.lowercase().contains(query) }
        }
}

sealed interface HighlightEvent {
    data object Refresh : HighlightEvent
    data object LoadScreenshots : HighlightEvent
    data class SelectScreenshot(val item: HighlightScreenshotItem) : HighlightEvent
    data class AnalyzeScreenshot(val item: HighlightScreenshotItem) : HighlightEvent
    data class UpdateSearchQuery(val query: String) : HighlightEvent
    data class TogglePackageFilter(val packageName: String?) : HighlightEvent
    data class SelectDate(val date: Instant?) : HighlightEvent
    data object ToggleAiOptIn : HighlightEvent
    data object GenerateReasoning : HighlightEvent
    data object SyncRealData : HighlightEvent
    data object ClearSearch : HighlightEvent
}

sealed interface HighlightEffect {
    data class ShowToast(val message: String) : HighlightEffect
    data class CopyToClipboard(val text: String) : HighlightEffect
}
