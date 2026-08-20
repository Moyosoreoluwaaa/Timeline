package com.timeline.domain

import kotlinx.datetime.Instant

data class Session(
    val id: String,
    val packageName: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationMinutes: Long = 0,
    val screenshots: List<String> = emptyList(),
    val segments: List<SessionSegment> = emptyList()
)

data class SessionSegment(
    val timestamp: Instant,
    val screenshotPath: String? = null,
    val activityDescription: String? = null
)
