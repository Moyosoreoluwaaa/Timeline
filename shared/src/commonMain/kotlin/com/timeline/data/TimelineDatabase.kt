package com.timeline.data

import androidx.room.*
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.timeline.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.datetime.Instant

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val userId: String? = null,
    val packageName: String,
    val startTime: Long,
    val endTime: Long?,
    val durationMinutes: Long,
    val screenshotsJson: String, // Simplified for this implementation
    val segmentsJson: String
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE packageName = :packageName ORDER BY startTime DESC")
    fun getSessionsByPackage(packageName: String): Flow<List<SessionEntity>>

    @Query("UPDATE sessions SET userId = :userId WHERE userId IS NULL")
    suspend fun associateAnonymousSessions(userId: String)
}

@Database(entities = [SessionEntity::class], version = 1)
abstract class TimelineDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}

expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<TimelineDatabase>

fun getDatabase(builder: RoomDatabase.Builder<TimelineDatabase>): TimelineDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .build()
}
