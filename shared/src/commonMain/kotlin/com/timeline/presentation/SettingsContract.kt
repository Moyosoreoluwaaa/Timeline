package com.timeline.presentation

import com.timeline.domain.model.TrialStatus
import com.timeline.domain.reasoning.HighlightReasoningMode

data class SettingsState(
    val excludedPackages: Set<String> = emptySet(),
    val availableApps: List<AppInfo> = emptyList(),
    val isUsageTrackingEnabled: Boolean = true,
    val isScreenshotCaptureEnabled: Boolean = true,
    val dataRetentionDays: Int = 30,
    val isLoggedIn: Boolean = false,
    val isPro: Boolean = false,
    val trialStatus: TrialStatus = TrialStatus.NOT_STARTED,
    val highlightReasoningMode: HighlightReasoningMode = HighlightReasoningMode.BALANCED,
    val digestFrequency: Int = 3,
    val digestHours: List<Int> = listOf(12, 17, 21)
)

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Any? = null,
    val isExcluded: Boolean
)

sealed interface SettingsEvent {
    data object LoadSettings : SettingsEvent
    data class ToggleExclusion(val packageName: String) : SettingsEvent
    data class SetUsageTracking(val enabled: Boolean) : SettingsEvent
    data class SetScreenshotCapture(val enabled: Boolean) : SettingsEvent
    data class SetDataRetention(val days: Int) : SettingsEvent
    data class SetReasoningMode(val mode: HighlightReasoningMode) : SettingsEvent
    data class SetDigestFrequency(val frequency: Int) : SettingsEvent
    data class SetDigestHours(val hours: List<Int>) : SettingsEvent
    data object Logout : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
}
