package com.timeline.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object TimeFormatter {
    fun formatTime(instant: Instant): String {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.hour}:${local.minute.toString().padStart(2, '0')}"
    }

    fun formatDate(instant: Instant): String {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${local.day}, ${local.year}"
    }
}
