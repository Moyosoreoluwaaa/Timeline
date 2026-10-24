package com.timeline.domain.ml

data class TextBlockItem(
    val text: String,
    val lines: List<String> = emptyList()
)

data class RecognizedTextResult(
    val fullText: String,
    val blocks: List<TextBlockItem> = emptyList(),
    val extractedKeywords: List<String> = emptyList()
)

data class RecognizedLabel(
    val text: String,
    val confidence: Float,
    val index: Int = 0
)

data class ImageAnalysisResult(
    val imagePath: String,
    val textResult: RecognizedTextResult,
    val labels: List<RecognizedLabel>,
    val timestamp: Long = 0L,
    val sessionOffsetMs: Long = 0L,
    val dwellDurationMs: Long = 0L,
    val visualCategory: String? = null,
    val categoryConfidence: Float = 0f,
    val confidenceScore: Float = 1.0f,
)

interface VisionAnalysisService {
    suspend fun analyzeImageFromPath(imagePath: String): Result<ImageAnalysisResult>
    fun isAvailable(): Boolean
}

class NoOpVisionAnalysisService : VisionAnalysisService {
    override suspend fun analyzeImageFromPath(imagePath: String): Result<ImageAnalysisResult> {
        return Result.failure(UnsupportedOperationException("Vision analysis not supported on this platform"))
    }
    override fun isAvailable(): Boolean = false
}
