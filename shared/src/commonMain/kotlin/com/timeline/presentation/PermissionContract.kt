package com.timeline.presentation

/** Number of pages inside the Intro step. */
const val INTRO_PAGE_COUNT = 5

enum class OnboardingStep {
    Opening,
    Intro,                 // 5-page carousel (replaces Welcome, ValueProposition, Storytelling, Customization)
    PermissionCardStack
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
    val currentStep: OnboardingStep = OnboardingStep.Opening,
    val stepHistory: List<OnboardingStep> = emptyList(),
    val activeCardIndex: Int = 0,
    val introPage: Int = 0,
    val focusAreas: Set<String> = emptySet(),
    val error: String? = null
)

sealed interface PermissionEvent {
    data object CheckPermissions : PermissionEvent
    data class GrantPermission(val id: String) : PermissionEvent
    data object NextStep : PermissionEvent
    data object PreviousStep : PermissionEvent
    data object RetryPermission : PermissionEvent
    data object SkipIntro : PermissionEvent
    data class ToggleFocusArea(val id: String) : PermissionEvent
}

sealed interface PermissionEffect {
    data object NavigateToUsageStatsSettings : PermissionEffect
    data object NavigateToOverlaySettings : PermissionEffect
    data object RequestNotificationPermission : PermissionEffect
    data object NavigateToAccessibilitySettings : PermissionEffect
    data object NavigateToBatteryOptimizationSettings : PermissionEffect
    data object AllGranted : PermissionEffect
}