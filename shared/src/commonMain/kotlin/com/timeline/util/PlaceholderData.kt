package com.timeline.util

import com.timeline.domain.Session
import com.timeline.domain.SessionSegment
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

object PlaceholderData {
    fun getDummySessions(): List<Session> {
        val now = Clock.System.now()
        return listOf(
            Session(
                id = "1",
                packageName = "com.android.settings",
                displayName = "Settings",
                startTime = now.minus(60.minutes),
                endTime = now.minus(50.minutes),
                durationMinutes = 10,
                segments = listOf(
                    SessionSegment(now.minus(58.minutes), activityDescription = "Main Settings"),
                    SessionSegment(now.minus(55.minutes), activityDescription = "Display Settings")
                )
            ),
            Session(
                id = "2",
                packageName = "com.google.android.youtube",
                displayName = "YouTube",
                startTime = now.minus(45.minutes),
                endTime = now.minus(15.minutes),
                durationMinutes = 30,
                segments = listOf(
                    SessionSegment(now.minus(40.minutes), activityDescription = "Watching video"),
                    SessionSegment(now.minus(25.minutes), activityDescription = "Searching")
                )
            ),
            Session(
                id = "3",
                packageName = "com.whatsapp",
                displayName = "WhatsApp",
                startTime = now.minus(10.minutes),
                endTime = null,
                durationMinutes = 10,
                segments = listOf(
                    SessionSegment(now.minus(8.minutes), activityDescription = "Chatting")
                )
            )
        )
    }
}
