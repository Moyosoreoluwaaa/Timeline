package com.timeline.presentation

import androidx.lifecycle.ViewModel
import com.timeline.domain.ExclusionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val exclusionPolicy: ExclusionPolicy
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.LoadApps -> {
                // In a real app, we'd query installed apps via a platform-specific provider
                // For now, we show the hardcoded ones to demonstrate the exclusion policy
                _state.value = SettingsState(
                    availableApps = listOf(
                        AppInfo("com.android.chrome", "Chrome", false),
                        AppInfo("com.google.android.youtube", "YouTube", false),
                        AppInfo("com.whatsapp", "WhatsApp", false),
                        AppInfo("com.instagram.android", "Instagram", false)
                    )
                )
            }
            is SettingsEvent.ToggleExclusion -> {
                // Logic to update exclusion policy
            }
        }
    }
}
