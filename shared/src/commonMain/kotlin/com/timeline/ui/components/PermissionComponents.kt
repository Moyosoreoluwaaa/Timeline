package com.timeline.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Shared icon mapping for permissions.
 */
fun iconForPermission(id: String): ImageVector = when (id) {
    "usage" -> Icons.Rounded.BarChart
    "overlay" -> Icons.Rounded.Layers
    "notifications" -> Icons.Rounded.Notifications
    "accessibility" -> Icons.Rounded.AccessibilityNew
    "battery" -> Icons.Rounded.BatteryChargingFull
    else -> Icons.Rounded.Extension
}
