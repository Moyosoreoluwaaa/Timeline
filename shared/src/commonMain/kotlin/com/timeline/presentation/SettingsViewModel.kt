package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.ExclusionPolicy
import com.timeline.domain.UserPreferences
import com.timeline.domain.NotificationManager
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.repository.AuthRepository
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.model.TrialStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock

class SettingsViewModel(
    private val exclusionPolicy: ExclusionPolicy,
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository,
    private val subscriptionManager: SubscriptionManager,
    private val notificationManager: NotificationManager,
    private val appInfoProvider: AppInfoProvider
) : ViewModel() {

    private val _availableApps = MutableStateFlow<List<AppBasicInfo>>(emptyList())

    val state: StateFlow<SettingsState> = combine(
        _availableApps,
        exclusionPolicy.getExcludedPackages(),
        userPreferences.state,
        subscriptionManager.isPro
    ) { apps, excluded, prefs, isPro ->
        SettingsState(
            excludedPackages = excluded,
            availableApps = apps.map { 
                AppInfo(it.packageName, it.name, it.icon, it.packageName in excluded)
            },
            isUsageTrackingEnabled = prefs.isUsageTrackingEnabled,
            isScreenshotCaptureEnabled = prefs.isScreenshotCaptureEnabled,
            dataRetentionDays = prefs.dataRetentionDays,
            isLoggedIn = prefs.isLoggedIn,
            isPro = isPro,
            trialStatus = calculateTrialStatus(prefs.trialStartedAt)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    private fun calculateTrialStatus(startedAt: Long?): TrialStatus {
        if (startedAt == null) return TrialStatus.NOT_STARTED
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsed = now - startedAt
        val trialDurationMs = 15L * 60L * 1000L
        return if (elapsed > trialDurationMs) {
            TrialStatus.EXPIRED
        } else {
            TrialStatus.ACTIVE
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.LoadSettings -> {
                viewModelScope.launch {
                    val apps = appInfoProvider.getInstalledApps()
                    _availableApps.value = apps.map { AppBasicInfo(it.packageName, it.name, it.icon) }
                }
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
            SettingsEvent.Logout -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    notificationManager.logout()
                    userPreferences.setLoggedIn(false)
                }
            }
        }
    }

    private data class AppBasicInfo(val packageName: String, val name: String, val icon: Any?)
}
