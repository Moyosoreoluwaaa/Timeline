package com.timeline.data

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<TimelineDatabase> {
    val dbFile = NSHomeDirectory() + "/timeline.db"
    return Room.databaseBuilder<TimelineDatabase>(
        name = dbFile,
        factory = {
            TimelineDatabase_Impl()
        }
    )
}
