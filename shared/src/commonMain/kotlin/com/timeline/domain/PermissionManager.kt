package com.timeline.domain

interface PermissionManager {
    fun hasUsageStatsPermission(): Boolean
    fun hasOverlayPermission(): Boolean
    fun hasNotificationPermission(): Boolean
    fun hasAccessibilityPermission(): Boolean
    fun isBatteryOptimizationDisabled(): Boolean
}
