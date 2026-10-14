package com.timeline.data

import androidx.room.RoomDatabaseConstructor

@Suppress(names = ["NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual object TimelineDatabaseConstructor :
    RoomDatabaseConstructor<TimelineDatabase> {
    actual override fun initialize(): TimelineDatabase {
        TODO("Not yet implemented")
    }
}