package com.timeline.presentation

data class SettingsState(
    val excludedPackages: Set<String> = emptySet(),
    val availableApps: List<AppInfo> = emptyList()
)

data class AppInfo(
    val packageName: String,
    val name: String,
    val isExcluded: Boolean
)

sealed interface SettingsEvent {
    data object LoadApps : SettingsEvent
    data class ToggleExclusion(val packageName: String) : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
}
