package com.timeline.data

import com.timeline.domain.Session
import com.timeline.domain.reasoning.AppDailyReasoning
import com.timeline.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

interface TimelineRepository {
    fun getTimeline(): Flow<List<Session>>
    fun getApplicationTimeline(packageName: String): Flow<List<Session>>
    suspend fun getSession(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun associateAnonymousSessions(userId: String)
    suspend fun migrateGuestSessions(userId: String, pathMap: Map<String, String>)

    fun getAppDailyReasoning(packageName: String, date: String): Flow<AppDailyReasoning?>
    suspend fun saveAppDailyReasoning(reasoning: AppDailyReasoning)
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineRepositoryImpl(
    private val sessionDao: SessionDao,
    private val reasoningDao: ReasoningDao,
    private val authRepository: AuthRepository
) : TimelineRepository {
    override fun getTimeline(): Flow<List<Session>> = 
        authRepository.currentUser.flatMapLatest { user ->
            sessionDao.getSessions(user?.uid).map { entities -> entities.map { it.toDomain() } }
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
