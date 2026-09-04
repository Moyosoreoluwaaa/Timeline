package com.timeline.presentation

import com.timeline.domain.model.TrialStatus

data class SettingsState(
    val excludedPackages: Set<String> = emptySet(),
    val availableApps: List<AppInfo> = emptyList(),
    val isUsageTrackingEnabled: Boolean = true,
    val isScreenshotCaptureEnabled: Boolean = true,
    val dataRetentionDays: Int = 30,
    val isLoggedIn: Boolean = false,
    val isPro: Boolean = false,
    val trialStatus: TrialStatus = TrialStatus.NOT_STARTED
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
    data object Logout : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
}
