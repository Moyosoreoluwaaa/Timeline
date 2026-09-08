package com.timeline.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE sessions ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_userId ON sessions(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_userId_startTime ON sessions(userId, startTime)")
    }
}

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<TimelineDatabase> {
    val appContext = context as? Context ?: throw IllegalArgumentException("Context required for Android database")
    val dbFile = appContext.getDatabasePath("timeline.db")
    return Room.databaseBuilder<TimelineDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).addMigrations(MIGRATION_1_2)
}
