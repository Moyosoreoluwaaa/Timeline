package com.timeline.ml

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.timeline.domain.ml.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidVisionAnalysisService(
    private val context: Context
) : VisionAnalysisService {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    override fun isAvailable(): Boolean = true

    override suspend fun analyzeImageFromPath(imagePath: String): Result<ImageAnalysisResult> = withContext(Dispatchers.IO) {
        runCatching {
            val inputImage = createInputImage(imagePath)
                ?: throw IllegalArgumentException("Image file not found for path: $imagePath")
            
            // Run Text Recognition OCR
            val visionText = try {
                awaitTask(textRecognizer.process(inputImage))
            } catch (e: Exception) {
                null
            }
            
            // Run Image Labeling
            val visionLabels = try {
                awaitTask(imageLabeler.process(inputImage))
            } catch (e: Exception) {
                emptyList()
            }
            
            val fullText = visionText?.text ?: ""

            val blocks = visionText?.textBlocks?.map { block ->
                TextBlockItem(
                    text = block.text,
                    lines = block.lines.map { it.text }
                )
            } ?: emptyList()
            
            // Extract meaningful keywords
            val stopWords = setOf(
                "the", "and", "that", "have", "for", "not", "with", "you", "this", "but",
                "his", "from", "they", "say", "her", "she", "will", "one", "all", "would",
                "there", "their", "what", "out", "about", "who", "get", "which", "when",
                "make", "can", "like", "time", "just", "him", "know", "take", "people",
                "into", "year", "your", "good", "some", "could", "them", "see", "other",
                "than", "then", "now", "look", "only", "come", "its", "over", "think", "also"
            )
            val extractedKeywords = if (fullText.isNotBlank()) {
                fullText
                    .split(Regex("[^a-zA-Z0-9_#@]"))
                    .asSequence()
                    .map { it.trim() }
                    .filter { it.length in 4..25 && it.lowercase() !in stopWords }
                    .distinctBy { it.lowercase() }
                    .take(15)
                    .toList()
            } else {
                emptyList()
            }

            // Parse Image Labels
            val recognizedLabels = visionLabels.map { label ->
                RecognizedLabel(
                    text = label.text,
                    confidence = label.confidence,
                    index = label.index
                )
            }.sortedByDescending { it.confidence }

            // Infer visual category and confidence from top labels
            val topLabel = recognizedLabels.firstOrNull()
            val (inferredCategory, inferredConfidence) = when {
                recognizedLabels.any { it.text.contains("chat", ignoreCase = true) || it.text.contains("message", ignoreCase = true) || it.text.contains("social", ignoreCase = true) } -> {
                    val label = recognizedLabels.first { it.text.contains("chat", ignoreCase = true) || it.text.contains("message", ignoreCase = true) || it.text.contains("social", ignoreCase = true) }
                    "Communication" to label.confidence
                }
                recognizedLabels.any { it.text.contains("software", ignoreCase = true) || it.text.contains("computer", ignoreCase = true) || it.text.contains("web", ignoreCase = true) || it.text.contains("document", ignoreCase = true) } -> {
                    val label = recognizedLabels.first { it.text.contains("software", ignoreCase = true) || it.text.contains("computer", ignoreCase = true) || it.text.contains("web", ignoreCase = true) || it.text.contains("document", ignoreCase = true) }
                    "Technical/Work" to label.confidence
                }
                recognizedLabels.any { it.text.contains("media", ignoreCase = true) || it.text.contains("video", ignoreCase = true) || it.text.contains("music", ignoreCase = true) || it.text.contains("photo", ignoreCase = true) } -> {
                    val label = recognizedLabels.first { it.text.contains("media", ignoreCase = true) || it.text.contains("video", ignoreCase = true) || it.text.contains("music", ignoreCase = true) || it.text.contains("photo", ignoreCase = true) }
                    "Media" to label.confidence
                }
                topLabel != null && topLabel.confidence >= 0.5f -> {
                    topLabel.text to topLabel.confidence
                }
                else -> {
                    null to 0f
                }
            }

            // Aggregate overall confidence score based on label confidence and text density
            val labelScore = recognizedLabels.firstOrNull()?.confidence ?: 0.6f
            val textScore = if (fullText.isNotBlank()) 1.0f else 0.4f
            val overallConfidence = ((labelScore * 0.4f) + (textScore * 0.6f)).coerceIn(0.0f, 1.0f)

            val textResult = RecognizedTextResult(
                fullText = fullText,
                blocks = blocks,
                extractedKeywords = extractedKeywords
            )

            ImageAnalysisResult(
                imagePath = imagePath,
                textResult = textResult,
                labels = recognizedLabels,
                timestamp = System.currentTimeMillis(),
                visualCategory = if (inferredConfidence >= 0.5f) inferredCategory else null,
                categoryConfidence = inferredConfidence,
                confidenceScore = overallConfidence
            )
        }
    }

    private fun createInputImage(imagePath: String): InputImage? {
        if (imagePath.isBlank()) return null

        if (imagePath.startsWith("content://")) {
            return try {
                InputImage.fromFilePath(context, Uri.parse(imagePath))
            } catch (e: Exception) {
                null
            }
        }

        val candidates = listOf(
            File(imagePath),
            File(context.filesDir, imagePath),
            File(context.filesDir, "screenshots/$imagePath"),
            File(context.cacheDir, imagePath)
        )

        val existingFile = candidates.firstOrNull { it.exists() && it.isFile }
        if (existingFile != null) {
            return try {
                InputImage.fromFilePath(context, Uri.fromFile(existingFile))
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }.addOnFailureListener { exception ->
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
    }
}
