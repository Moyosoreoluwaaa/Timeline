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

@Entity(tableName = "app_daily_reasoning", primaryKeys = ["packageName", "date"])
data class ReasoningEntity(
    val packageName: String,
    val date: String, // format: YYYY-MM-DD
    val summary: String,
    val cognitiveMode: String,
    val actionItemsJson: String, // JSON / split list of strings
    val lastUpdated: Long = 0L
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE (:userId IS NULL AND userId IS NULL) OR (userId = :userId) ORDER BY startTime DESC")
    fun getSessions(userId: String?): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

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

@Dao
interface ReasoningDao {
    @Query("SELECT * FROM app_daily_reasoning WHERE packageName = :packageName AND date = :date")
    fun getReasoning(packageName: String, date: String): Flow<ReasoningEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReasoning(reasoning: ReasoningEntity)

    @Query("DELETE FROM app_daily_reasoning WHERE lastUpdated < :threshold")
    suspend fun purgeOldReasoning(threshold: Long)
}

@Entity(tableName = "analysis_results")
data class AnalysisResultEntity(
    @PrimaryKey val screenshotId: String,
    val redactedFullText: String,
    val keywordsJson: String,
    val labelsJson: String,
    val visualCategory: String?,
    val categoryConfidence: Float,
    val confidenceScore: Float,
    val timestamp: Long
)

@Dao
interface AnalysisResultDao {
    @Query("SELECT * FROM analysis_results WHERE screenshotId = :screenshotId")
    suspend fun getAnalysis(screenshotId: String): AnalysisResultEntity?

    @Query("SELECT * FROM analysis_results WHERE screenshotId IN (:screenshotIds)")
    suspend fun getAnalyses(screenshotIds: List<String>): List<AnalysisResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisResultEntity)

    @Query("DELETE FROM analysis_results WHERE timestamp < :threshold")
    suspend fun purgeOldAnalysis(threshold: Long)
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

@Database(entities = [SessionEntity::class, ReasoningEntity::class, AnalysisResultEntity::class], version = 4)
@ConstructedBy(TimelineDatabaseConstructor::class)
abstract class TimelineDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun reasoningDao(): ReasoningDao
    abstract fun analysisResultDao(): AnalysisResultDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object TimelineDatabaseConstructor : RoomDatabaseConstructor<TimelineDatabase> {
    override fun initialize(): TimelineDatabase
}

expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<TimelineDatabase>

fun getDatabase(builder: RoomDatabase.Builder<TimelineDatabase>): TimelineDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .build()
}
