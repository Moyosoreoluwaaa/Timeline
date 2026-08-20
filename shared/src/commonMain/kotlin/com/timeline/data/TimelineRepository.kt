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
        startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(startTime),
        endTime = endTime?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
        durationMinutes = durationMinutes,
        screenshots = emptyList(), // JSON parsing logic omitted for brevity
        segments = emptyList()
    )

    private fun Session.toEntity(): SessionEntity = SessionEntity(
        id = id,
        packageName = packageName,
        startTime = startTime.toEpochMilliseconds(),
        endTime = endTime?.toEpochMilliseconds(),
        durationMinutes = durationMinutes,
        screenshotsJson = "",
        segmentsJson = ""
    )
}
