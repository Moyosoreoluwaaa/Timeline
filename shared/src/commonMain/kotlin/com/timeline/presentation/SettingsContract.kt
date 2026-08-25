package com.timeline.presentation

data class SettingsState(
    val excludedPackages: Set<String> = emptySet(),
    val availableApps: List<AppInfo> = emptyList(),
    val isUsageTrackingEnabled: Boolean = true,
    val isScreenshotCaptureEnabled: Boolean = true,
    val dataRetentionDays: Int = 30
)

data class AppInfo(
    val packageName: String,
    val name: String,
    val isExcluded: Boolean
)

sealed interface SettingsEvent {
    data object LoadSettings : SettingsEvent
    data class ToggleExclusion(val packageName: String) : SettingsEvent
    data class SetUsageTracking(val enabled: Boolean) : SettingsEvent
    data class SetScreenshotCapture(val enabled: Boolean) : SettingsEvent
    data class SetDataRetention(val days: Int) : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
}
