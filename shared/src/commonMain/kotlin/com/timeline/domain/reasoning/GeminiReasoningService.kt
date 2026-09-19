package com.timeline.domain.reasoning

import com.timeline.domain.SecretConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import co.touchlab.kermit.Logger

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null,
)

@Serializable
private data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String
)

@Serializable
private data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json",
    val temperature: Float = 0.7f
)

@Serializable
private data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent
)

class GeminiReasoningService(private val logger: Logger) : ReasoningService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
    }

    override fun performLocalHeuristics(ocrTexts: List<String>): LocalEntities {
        // Fallback to local service logic if needed, but this class is for Cloud
        return LocalHeuristicService().performLocalHeuristics(ocrTexts)
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
        val apiKey = SecretConstants.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "your_gemini_api_key_here") {
            logger.w { "Gemini API key missing, falling back to local heuristic reasoning" }
            return LocalHeuristicService().generateDailyNarrative(
                packageName,
                appName,
                date,
                ocrDumps,
                labels,
                previousDaySummary,
                mode
            )
        }

        val primaryUrl =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"
        val fallbackUrl =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"

        // Ensure text is sanitized via PiiRedactor before cloud dispatch
        val sanitizedDumps = PiiRedactor.redactAll(ocrDumps)
        val combinedText = sanitizedDumps.joinToString("\n---\n")
        val combinedLabels = labels.asSequence().distinct().joinToString(", ")

        val modeInstruction = when (mode) {
            HighlightReasoningMode.CONCISE -> "Provide a very brief, 1-sentence summary of the main activity. Minimize details."
            HighlightReasoningMode.BALANCED -> "Provide a clear, 2-sentence summary outlining the main activity and its purpose."
            HighlightReasoningMode.EXPLANATORY -> "Provide a detailed analytical narrative (3-4 sentences) exploring the workflow, context, and focus of this activity."
        }

        val systemPrompt = """
            You are an activity reasoning engine for a privacy-focused timeline app.
            Your task is to turn raw OCR text and image labels from a specific app's usage into a high-quality human narrative.
            
            $modeInstruction
            
            Return a JSON object with the following structure:
            {
              "summary": "The generated narrative.",
              "cognitiveMode": "One of: Deep Focus, Communication, Information Seeking, Administrative, Creative, or Casual.",
              "actionItems": ${if (mode == HighlightReasoningMode.CONCISE) "[]" else "[\"List of detected tasks or follow-ups, or empty list if none.\"]"}
            }
            
            Rules:
            1. Be objective and concise.
            2. Ignore UI noise like 'Back', 'Share', 'Settings' unless it provides context.
            3. If the data is too sparse, provide a generic but accurate summary.
        """.trimIndent()

        val userPrompt = buildString {
            appendLine("App: $appName ($packageName)")
            appendLine("Date: $date")
            if (!previousDaySummary.isNullOrBlank() && mode != HighlightReasoningMode.CONCISE) {
                appendLine("Context From Yesterday: $previousDaySummary")
            }
            appendLine("Image Labels: $combinedLabels")
            appendLine()
            appendLine("OCR Text Samples:")
            appendLine(combinedText)
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig()
        )

        return try {
            var response: HttpResponse = client.post(primaryUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status != HttpStatusCode.OK) {
                logger.w { "Primary model gemini-3.8-flash returned ${response.status}. Trying fallback gemini-3.6-flash..." }
                response = client.post(fallbackUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val geminiResponse = response.body<GeminiResponse>()
                val rawText =
                    geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: return LocalHeuristicService().generateDailyNarrative(
                            packageName,
                            appName,
                            date,
                            ocrDumps,
                            labels,
                            previousDaySummary,
                            mode
                        )

                // Clean markdown code fence if present
                val cleanedJson = rawText
                    .trim()
                    .removePrefix("```json")
                    .removePrefix("```JSON")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = Json { ignoreUnknownKeys = true }
                val parsed = json.decodeFromString<JsonObject>(cleanedJson)

                logger.i { "Gemini narrative generated successfully for $appName: ${parsed["summary"]?.jsonPrimitive?.content}" }
                Result.success(
                    AppDailyReasoning(
                        packageName = packageName,
                        date = date,
                        summary = parsed["summary"]?.jsonPrimitive?.content ?: "Activity recorded",
                        cognitiveMode = parsed["cognitiveMode"]?.jsonPrimitive?.content ?: "Deep Focus",
                        actionItems = parsed["actionItems"]?.jsonArray?.map { it.jsonPrimitive.content }
                            ?: emptyList()
                    ))
            } else {
                val errorBody = response.bodyAsText()
                logger.w { "Gemini API returned ${response.status}: $errorBody. Falling back to local reasoning." }
                LocalHeuristicService().generateDailyNarrative(
                    packageName,
                    appName,
                    date,
                    ocrDumps,
                    labels,
                    previousDaySummary,
                    mode
                )
            }
        } catch (e: Exception) {
            logger.w(e) { "Failed to call Gemini API. Falling back to local reasoning." }
            LocalHeuristicService().generateDailyNarrative(
                packageName,
                appName,
                date,
                ocrDumps,
                labels,
                previousDaySummary,
                mode
            )
        }
    }
}