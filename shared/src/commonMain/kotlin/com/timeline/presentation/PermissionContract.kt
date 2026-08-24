package com.timeline.presentation

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean = false,
    val isVerifying: Boolean = false
)

data class PermissionState(
    val permissions: List<PermissionItem> = emptyList(),
    val allGranted: Boolean = false
)

sealed interface PermissionEvent {
    data object CheckPermissions : PermissionEvent
    data class GrantPermission(val id: String) : PermissionEvent
    data object StartTracking : PermissionEvent
}

sealed interface PermissionEffect {
    data object NavigateToUsageStatsSettings : PermissionEffect
    data object NavigateToOverlaySettings : PermissionEffect
    data object RequestNotificationPermission : PermissionEffect
    data object NavigateToAccessibilitySettings : PermissionEffect
    data object NavigateToBatteryOptimizationSettings : PermissionEffect
    data object AllGranted : PermissionEffect
}
