package com.timeline.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<TimelineDatabase> {
    val appContext = context as? Context ?: throw IllegalArgumentException("Context required for Android database")
    val dbFile = appContext.getDatabasePath("timeline.db")
    return Room.databaseBuilder<TimelineDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
