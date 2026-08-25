package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val exclusionPolicy: ExclusionPolicy,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _availableApps = MutableStateFlow<List<AppBasicInfo>>(emptyList())

    val state: StateFlow<SettingsState> = combine(
        _availableApps,
        exclusionPolicy.getExcludedPackages(),
        userPreferences.state
    ) { apps, excluded, prefs ->
        SettingsState(
            excludedPackages = excluded,
            availableApps = apps.map { 
                AppInfo(it.packageName, it.name, it.packageName in excluded)
            },
            isUsageTrackingEnabled = prefs.isUsageTrackingEnabled,
            isScreenshotCaptureEnabled = prefs.isScreenshotCaptureEnabled,
            dataRetentionDays = prefs.dataRetentionDays
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.LoadSettings -> {
                // In a real app, we'd query installed apps via a platform-specific provider
                _availableApps.value = listOf(
                    AppBasicInfo("com.android.chrome", "Chrome"),
                    AppBasicInfo("com.google.android.youtube", "YouTube"),
                    AppBasicInfo("com.whatsapp", "WhatsApp"),
                    AppBasicInfo("com.instagram.android", "Instagram")
                )
            }
            is SettingsEvent.ToggleExclusion -> {
                viewModelScope.launch {
                    exclusionPolicy.toggleExclusion(event.packageName)
                }
            }
            is SettingsEvent.SetUsageTracking -> {
                viewModelScope.launch {
                    userPreferences.setUsageTrackingEnabled(event.enabled)
                }
            }
            is SettingsEvent.SetScreenshotCapture -> {
                viewModelScope.launch {
                    userPreferences.setScreenshotCaptureEnabled(event.enabled)
                }
            }
            is SettingsEvent.SetDataRetention -> {
                viewModelScope.launch {
                    userPreferences.setDataRetentionDays(event.days)
                }
            }
        }
    }

    private data class AppBasicInfo(val packageName: String, val name: String)
}
