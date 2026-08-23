package com.timeline.presentation

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean = false,
    val isVerifying: Boolean = false
)

data class PermissionState(
    val permissions: List<PermissionItem> = listOf(
        PermissionItem("usage", "Usage access", "Allows Everett to track app usage."),
        PermissionItem("overlay", "Display over other apps", "Allows Everett to capture screenshots in the background."),
        PermissionItem("notifications", "Notifications", "Allows Everett to send notifications."),
        PermissionItem("accessibility", "Accessibility Service", "Required for advanced activity tracking."),
        PermissionItem("battery", "Battery Optimization", "Prevents the system from revoking permissions.")
    ),
    val allGranted: Boolean = false
)

sealed interface PermissionEvent {
    data class GrantPermission(val id: String) : PermissionEvent
}

sealed interface PermissionEffect {
    data object AllGranted : PermissionEffect
}
