package com.timeline.data

import androidx.room.*
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.IO

@Entity(
    tableName = "sessions",
    indices = [
        Index("userId"),
        Index(value = ["userId", "startTime"])
    ]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    val userId: String? = null,
    val packageName: String,
    val startTime: Long,
    val endTime: Long?,
    val durationMinutes: Long,
    val screenshotsJson: String,
    val segmentsJson: String,
    val updatedAt: Long = 0L,
    val isSynced: Boolean = false
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE (:userId IS NULL AND userId IS NULL) OR (userId = :userId) ORDER BY startTime DESC")
    fun getSessions(userId: String?): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE ((:userId IS NULL AND userId IS NULL) OR (userId = :userId)) AND packageName = :packageName ORDER BY startTime DESC")
    fun getSessionsByPackage(packageName: String, userId: String?): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE userId IS NULL")
    suspend fun getGuestSessionsSync(): List<SessionEntity>

    @Update
    suspend fun updateSessions(sessions: List<SessionEntity>)

    @Transaction
    suspend fun migrateGuestSessionsTransactional(userId: String, pathMap: Map<String, String>, updatedAt: Long) {
        val guestSessions = getGuestSessionsSync()
        val updatedSessions = guestSessions.map { session ->
            val newScreenshotsJson = updateJsonPaths(session.screenshotsJson, pathMap)
            val newSegmentsJson = updateJsonPaths(session.segmentsJson, pathMap)
            session.copy(
                userId = userId,
                screenshotsJson = newScreenshotsJson,
                segmentsJson = newSegmentsJson,
                updatedAt = updatedAt,
                isSynced = false
            )
        }
        updateSessions(updatedSessions)
    }

    @Query("DELETE FROM sessions WHERE userId = :userId")
    suspend fun deleteSessionsForUser(userId: String?)
}

private fun updateJsonPaths(json: String, pathMap: Map<String, String>): String {
    if (json.isBlank()) return json
    var result = json
    for ((oldPath, newPath) in pathMap) {
        if (oldPath.isNotBlank() && newPath.isNotBlank()) {
            result = result.replace(oldPath, newPath)
        }
    }
    return result
}

@Database(entities = [SessionEntity::class], version = 2)
@ConstructedBy(TimelineDatabaseConstructor::class)
abstract class TimelineDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object TimelineDatabaseConstructor : RoomDatabaseConstructor<TimelineDatabase>

expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<TimelineDatabase>

fun getDatabase(builder: RoomDatabase.Builder<TimelineDatabase>): TimelineDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .build()
}
