package com.timeline.data

import com.timeline.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TimelineRepository {
    fun getTimeline(): Flow<List<Session>>
    fun getApplicationTimeline(packageName: String): Flow<List<Session>>
    suspend fun getSession(id: String): Session?
    suspend fun saveSession(session: Session)
}

class TimelineRepositoryImpl(private val dao: SessionDao) : TimelineRepository {
    override fun getTimeline(): Flow<List<Session>> = 
        dao.getAllSessions().map { entities -> entities.map { it.toDomain() } }

    override fun getApplicationTimeline(packageName: String): Flow<List<Session>> =
        dao.getSessionsByPackage(packageName).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getSession(id: String): Session? =
        dao.getSessionById(id)?.toDomain()

    override suspend fun saveSession(session: Session) {
        dao.insertSession(session.toEntity())
    }

    private fun SessionEntity.toDomain(): Session = Session(
        id = id,
        packageName = packageName,
        startTime = kotlin.time.Instant.fromEpochMilliseconds(startTime),
        endTime = endTime?.let { kotlin.time.Instant.fromEpochMilliseconds(it) },
        durationMinutes = durationMinutes,
        screenshots = screenshotsJson.split("|||").filter { it.isNotBlank() },
        segments = deserializeSegments(segmentsJson)
    )

    private fun Session.toEntity(): SessionEntity = SessionEntity(
        id = id,
        packageName = packageName,
        startTime = startTime.toEpochMilliseconds(),
        endTime = endTime?.toEpochMilliseconds(),
        durationMinutes = durationMinutes,
        screenshotsJson = screenshots.joinToString("|||"),
        segmentsJson = serializeSegments(segments)
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
