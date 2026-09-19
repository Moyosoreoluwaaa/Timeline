package com.timeline.data

import com.timeline.domain.Session
import com.timeline.domain.ml.ImageAnalysisResult
import com.timeline.domain.ml.RecognizedLabel
import com.timeline.domain.ml.RecognizedTextResult
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.reasoning.PiiRedactor
import com.timeline.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

interface TimelineRepository {
    fun getTimeline(): Flow<List<Session>>
    suspend fun insertSessions(sessions: List<Session>)
    fun getApplicationTimeline(packageName: String): Flow<List<Session>>
    suspend fun getSession(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun seedMockData()
    suspend fun associateAnonymousSessions(userId: String)
    suspend fun migrateGuestSessions(userId: String, pathMap: Map<String, String>)

    fun getAppDailyReasoning(packageName: String, date: String): Flow<AppDailyReasoning?>
    suspend fun saveAppDailyReasoning(reasoning: AppDailyReasoning)

    suspend fun getAnalysisResult(screenshotId: String): ImageAnalysisResult?
    suspend fun saveAnalysisResult(screenshotId: String, result: ImageAnalysisResult)
    suspend fun purgeOldAnalysisResults(thresholdTimestamp: Long)
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineRepositoryImpl(
    private val sessionDao: SessionDao,
    private val piiRedactor: PiiRedactor,
    private val reasoningDao: ReasoningDao,
    private val analysisResultDao: AnalysisResultDao,
    private val authRepository: AuthRepository,
) : TimelineRepository {
    override fun getTimeline(): Flow<List<Session>> = 
        authRepository.currentUser.flatMapLatest { user ->
            sessionDao.getSessions(user?.uid).map { entities -> entities.map { it.toDomain() } }
        }

    override suspend fun insertSessions(sessions: List<Session>) {
        val entities = sessions.map { session ->
            val redactedSegments = session.segments.map { segment ->
                segment.copy(
                    activityDescription = segment.activityDescription?.let { piiRedactor.redact(it) }
                )
            }
            session.copy(segments = redactedSegments).toEntity()
        }
        sessionDao.insertSessions(entities)
    }

    override fun getApplicationTimeline(packageName: String): Flow<List<Session>> =
        authRepository.currentUser.flatMapLatest { user ->
            sessionDao.getSessionsByPackage(packageName, user?.uid).map { entities -> entities.map { it.toDomain() } }
        }

    override suspend fun getSession(id: String): Session? =
        sessionDao.getSessionById(id)?.toDomain()

    override suspend fun saveSession(session: Session) {
        val currentUid = session.userId ?: authRepository.getCurrentUser()?.uid
        sessionDao.insertSession(session.toEntity(currentUid))
    }

    override suspend fun seedMockData() {
        val now = Clock.System.now()
        val mockSession = Session(
            id = "mock_session_101",
            userId = authRepository.getCurrentUser()?.uid,
            packageName = "com.timeline.demo",
            startTime = now,
            endTime = now,
            durationMinutes = 15,
            screenshots = listOf("mock_screenshot_1.jpg"),
            segments = listOf(
                com.timeline.domain.SessionSegment(
                    timestamp = now,
                    screenshotPath = "mock_screenshot_1.jpg",
                    activityDescription = "Initial tutorial walkthrough session"
                )
            )
        )
        saveSession(mockSession)
    }

    override suspend fun associateAnonymousSessions(userId: String) {
        sessionDao.migrateGuestSessionsTransactional(
            userId = userId,
            pathMap = emptyMap(),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun migrateGuestSessions(userId: String, pathMap: Map<String, String>) {
        sessionDao.migrateGuestSessionsTransactional(
            userId = userId,
            pathMap = pathMap,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    override fun getAppDailyReasoning(packageName: String, date: String): Flow<AppDailyReasoning?> {
        return reasoningDao.getReasoning(packageName, date).map { entity ->
            entity?.let {
                AppDailyReasoning(
                    packageName = it.packageName,
                    date = it.date,
                    summary = it.summary,
                    cognitiveMode = it.cognitiveMode,
                    actionItems = it.actionItemsJson.split("|||").filter { item -> item.isNotBlank() }
                )
            }
        }
    }

    override suspend fun saveAppDailyReasoning(reasoning: AppDailyReasoning) {
        reasoningDao.insertReasoning(
            ReasoningEntity(
                packageName = reasoning.packageName,
                date = reasoning.date,
                summary = reasoning.summary,
                cognitiveMode = reasoning.cognitiveMode,
                actionItemsJson = reasoning.actionItems.joinToString("|||"),
                lastUpdated = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun getAnalysisResult(screenshotId: String): ImageAnalysisResult? {
        val entity = analysisResultDao.getAnalysis(screenshotId) ?: return null
        val keywords = entity.keywordsJson.split("|||").filter { it.isNotBlank() }
        val labels = if (entity.labelsJson.isBlank()) emptyList() else {
            entity.labelsJson.split("|||").mapNotNull { line ->
                val parts = line.split(":::")
                if (parts.size >= 3) {
                    RecognizedLabel(
                        text = parts[0],
                        confidence = parts[1].toFloatOrNull() ?: 0f,
                        index = parts[2].toIntOrNull() ?: 0
                    )
                } else null
            }
        }
        return ImageAnalysisResult(
            imagePath = screenshotId,
            textResult = RecognizedTextResult(
                fullText = entity.redactedFullText,
                extractedKeywords = keywords
            ),
            labels = labels,
            timestamp = entity.timestamp,
            visualCategory = entity.visualCategory,
            categoryConfidence = entity.categoryConfidence,
            confidenceScore = entity.confidenceScore
        )
    }

    override suspend fun saveAnalysisResult(screenshotId: String, result: ImageAnalysisResult) {
        // Redact PII in-memory before storing into Room database (Zero unredacted storage guarantee)
        val sanitizedText = PiiRedactor.redact(result.textResult.fullText)
        val sanitizedKeywords = result.textResult.extractedKeywords
            .asSequence()
            .map { PiiRedactor.redact(it) }
            .filter { !it.contains("[") && it.isNotBlank() }
            .toList()
        val keywordsJson = sanitizedKeywords.joinToString("|||")
        val labelsJson = result.labels.joinToString("|||") { "${it.text}:::${it.confidence}:::${it.index}" }

        analysisResultDao.insertAnalysis(
            AnalysisResultEntity(
                screenshotId = screenshotId,
                redactedFullText = sanitizedText,
                keywordsJson = keywordsJson,
                labelsJson = labelsJson,
                visualCategory = result.visualCategory,
                categoryConfidence = result.categoryConfidence,
                confidenceScore = result.confidenceScore,
                timestamp = result.timestamp.takeIf { it > 0 } ?: Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun purgeOldAnalysisResults(thresholdTimestamp: Long) {
        analysisResultDao.purgeOldAnalysis(thresholdTimestamp)
    }

    private fun SessionEntity.toDomain(): Session = Session(
        id = id,
        userId = userId,
        packageName = packageName,
        startTime = kotlin.time.Instant.fromEpochMilliseconds(startTime),
        endTime = endTime?.let { kotlin.time.Instant.fromEpochMilliseconds(it) },
        durationMinutes = durationMinutes,
        screenshots = screenshotsJson.split("|||").filter { it.isNotBlank() },
        segments = deserializeSegments(segmentsJson)
    )

    private fun Session.toEntity(ownerUserId: String? = null): SessionEntity = SessionEntity(
        id = id,
        userId = ownerUserId,
        packageName = packageName,
        startTime = startTime.toEpochMilliseconds(),
        endTime = endTime?.toEpochMilliseconds(),
        durationMinutes = durationMinutes,
        screenshotsJson = screenshots.joinToString("|||"),
        segmentsJson = serializeSegments(segments),
        updatedAt = Clock.System.now().toEpochMilliseconds()
    )

    private fun serializeSegments(segments: List<com.timeline.domain.SessionSegment>): String {
        return segments.joinToString("\n") { segment ->
            val timestamp = segment.timestamp.toEpochMilliseconds()
            val path = segment.screenshotPath ?: ""
            val desc = segment.activityDescription ?: ""
            "$timestamp|$path|$desc"
        }
    }

    private fun deserializeSegments(json: String): List<com.timeline.domain.SessionSegment> {
        if (json.isBlank()) return emptyList()
        return json.split("\n").filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 3) {
                com.timeline.domain.SessionSegment(
                    timestamp = kotlin.time.Instant.fromEpochMilliseconds(parts[0].toLongOrNull() ?: 0L),
                    screenshotPath = parts[1].ifBlank { null },
                    activityDescription = parts[2].ifBlank { null }
                )
            } else null
        }
    }
}
