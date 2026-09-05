package com.timeline.presentation

enum class OnboardingStep {
    Welcome,
    ValueProp,
    PermissionOverview,
    AccessibilityIntro,
    AccessibilityGrant,
    AccessibilitySuccess,
    AccessibilityFailure,
    UsageIntro,
    UsageGrant,
    UsageSuccess,
    UsageFailure,
    NotificationsIntro,
    NotificationsGrant,
    AllSet
}

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean = false,
    val isVerifying: Boolean = false,
    val illustration: Any? = null
)

data class PermissionState(
    val permissions: List<PermissionItem> = emptyList(),
    val allGranted: Boolean = false,
    val currentStep: OnboardingStep = OnboardingStep.Welcome
)

sealed interface PermissionEvent {
    data object CheckPermissions : PermissionEvent
    data class GrantPermission(val id: String) : PermissionEvent
    data object StartTracking : PermissionEvent
    data object NextStep : PermissionEvent
    data object PreviousStep : PermissionEvent
    data object RetryPermission : PermissionEvent
}

sealed interface PermissionEffect {
    data object NavigateToUsageStatsSettings : PermissionEffect
    data object NavigateToOverlaySettings : PermissionEffect
    data object RequestNotificationPermission : PermissionEffect
    data object NavigateToAccessibilitySettings : PermissionEffect
    data object NavigateToBatteryOptimizationSettings : PermissionEffect
    data object AllGranted : PermissionEffect
}
