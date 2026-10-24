package com.timeline.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object TimeFormatter {
    fun formatTime(instant: Instant): String {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = local.hour
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        val minute = local.minute.toString().padStart(2, '0')
        return "$displayHour:$minute $amPm"
    }

    fun formatDate(instant: Instant): String {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${local.day}, ${local.year}"
    }
}
