package com.timeline.presentation

import com.timeline.domain.ml.ImageAnalysisResult
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.reasoning.HighlightReasoningMode
import com.timeline.domain.reasoning.LocalEntities
import kotlin.time.Instant

/**
 * Screenshot item representation for the raw developer-level highlight diagnostic inspector.
 */
data class RawHighlightScreenshotItem(
    val id: String,
    val sessionId: String,
    val packageName: String,
    val displayName: String?,
    val icon: Any?,
    val screenshotPath: String,
    val timestamp: Instant,
    val durationMinutes: Long = 0
)

/**
 * Comprehensive diagnostic state holding raw ML extractions, PII redactions, and app-level reasonings.
 */
data class RawHighlightState(
    val screenshots: List<RawHighlightScreenshotItem> = emptyList(),
    val selectedScreenshot: RawHighlightScreenshotItem? = null,
    val selectedPackage: String? = null,
    val selectedDate: Instant? = null,
    val isAnalyzing: Boolean = false,
    val isRefreshing: Boolean = false,
    val analyzingPaths: Set<String> = emptySet(),
    val analysisCache: Map<String, ImageAnalysisResult> = emptyMap(),
    val localEntitiesCache: Map<String, LocalEntities> = emptyMap(),
    val currentResult: ImageAnalysisResult? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val appDailyReasonings: Map<String, AppDailyReasoning> = emptyMap(),
    val isReasoningLoading: Boolean = false,
    val isAgentOptedIn: Boolean = false,
    val isPro: Boolean = false,
    val isUsagePermissionGranted: Boolean = true,
    val isSyncingDeviceUsage: Boolean = false,
    val reasoningStage: ReasoningStage = ReasoningStage.IDLE,
    val isPrivacyShieldActive: Boolean = false,
    val categoryIcon: String? = null,
    val reasoningProgress: Float = 0f,
    val currentReasoningMode: HighlightReasoningMode = HighlightReasoningMode.BALANCED
) {
    enum class ReasoningStage {
        IDLE, REDACTING, DEDUPLICATING, CONTEXTUALIZING, REASONING
    }

    val filteredScreenshots: List<RawHighlightScreenshotItem>
        get() = if (selectedPackage != null) {
            screenshots.filter { it.packageName == selectedPackage }
        } else {
            screenshots
        }

    val uniqueAppItems: List<RawHighlightScreenshotItem>
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

sealed interface RawHighlightEvent {
    data object Refresh : RawHighlightEvent
    data object LoadScreenshots : RawHighlightEvent
    data class SelectScreenshot(val item: RawHighlightScreenshotItem) : RawHighlightEvent
    data class AnalyzeScreenshot(val item: RawHighlightScreenshotItem) : RawHighlightEvent
    data class UpdateSearchQuery(val query: String) : RawHighlightEvent
    data class TogglePackageFilter(val packageName: String?) : RawHighlightEvent
    data class SelectDate(val date: Instant?) : RawHighlightEvent
    data object ToggleAgentOptIn : RawHighlightEvent
    data object GenerateReasoning : RawHighlightEvent
    data object SyncRealData : RawHighlightEvent
    data object ClearSearch : RawHighlightEvent
    data class SetReasoningMode(val mode: HighlightReasoningMode) : RawHighlightEvent
}

sealed interface RawHighlightEffect {
    data class ShowToast(val message: String) : RawHighlightEffect
    data class CopyToClipboard(val text: String) : RawHighlightEffect
}

