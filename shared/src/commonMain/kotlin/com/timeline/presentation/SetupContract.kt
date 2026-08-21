package com.timeline.presentation

data class SetupState(
    val isUsageStatsGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isNotificationGranted: Boolean = false,
    val isAccessibilityGranted: Boolean = false,
    val isTrackingStarted: Boolean = false
)

sealed interface SetupEvent {
    data object CheckPermissions : SetupEvent
    data object RequestUsageStats : SetupEvent
    data object RequestOverlay : SetupEvent
    data object RequestNotification : SetupEvent
    data object RequestAccessibility : SetupEvent
    data object StartTracking : SetupEvent
}

sealed interface SetupEffect {
    data object NavigateToUsageStatsSettings : SetupEffect
    data object NavigateToOverlaySettings : SetupEffect
    data object RequestNotificationPermission : SetupEffect
    data object NavigateToAccessibilitySettings : SetupEffect
    data object AllPermissionsGranted : SetupEffect
}
