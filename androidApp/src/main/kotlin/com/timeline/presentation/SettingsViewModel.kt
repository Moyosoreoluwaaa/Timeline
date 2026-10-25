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

import com.timeline.analytics.Analytics
import com.timeline.analytics.AnalyticsEvents
import com.timeline.analytics.AnalyticsParams

class SettingsViewModel(
    private val exclusionPolicy: ExclusionPolicy,
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository,
    subscriptionManager: SubscriptionManager,
    private val notificationManager: NotificationManager,
    private val appInfoProvider: AppInfoProvider,
    private val analytics: Analytics
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
            trialStatus = calculateTrialStatus(prefs.trialStartedAt),
            highlightReasoningMode = prefs.highlightReasoningMode,
            digestFrequency = prefs.digestFrequency,
            digestHours = prefs.digestHours,
            highlightVariant = prefs.experiments.highlightVariant,
            appVersion = appInfoProvider.getAppVersionName()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState(appVersion = appInfoProvider.getAppVersionName()))

    private fun calculateTrialStatus(startedAt: Long?): TrialStatus {
        return MetricsAggregator.calculateTrialStatus(startedAt)
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
                    val excluded = exclusionPolicy.getExcludedPackages().firstOrNull() ?: emptySet()
                    analytics.log(
                        AnalyticsEvents.EXCLUSION_LIST_CHANGED,
                        mapOf(AnalyticsParams.EXCLUDED_COUNT to excluded.size)
                    )
                }
            }
            is SettingsEvent.SetUsageTracking -> {
                viewModelScope.launch {
                    userPreferences.setUsageTrackingEnabled(event.enabled)
                    analytics.log(
                        AnalyticsEvents.CAPTURE_TOGGLED,
                        mapOf(AnalyticsParams.ENABLED to event.enabled)
                    )
                }
            }
            is SettingsEvent.SetScreenshotCapture -> {
                viewModelScope.launch {
                    userPreferences.setScreenshotCaptureEnabled(event.enabled)
                    analytics.log(
                        AnalyticsEvents.CAPTURE_TOGGLED,
                        mapOf(AnalyticsParams.ENABLED to event.enabled)
                    )
                }
            }
            is SettingsEvent.SetDataRetention -> {
                viewModelScope.launch {
                    userPreferences.setDataRetentionDays(event.days)
                    analytics.log(
                        AnalyticsEvents.RETENTION_CHANGED,
                        mapOf(AnalyticsParams.DAYS to event.days)
                    )
                }
            }
            is SettingsEvent.SetReasoningMode -> {
                viewModelScope.launch {
                    userPreferences.setHighlightReasoningMode(event.mode)
                }
            }
            is SettingsEvent.SetDigestFrequency -> {
                viewModelScope.launch {
                    val currentHours = state.value.digestHours
                    userPreferences.setDigestSchedule(event.frequency, currentHours)
                }
            }
            is SettingsEvent.SetDigestHours -> {
                viewModelScope.launch {
                    val currentFreq = state.value.digestFrequency
                    userPreferences.setDigestSchedule(currentFreq, event.hours)
                }
            }
            is SettingsEvent.SetHighlightVariant -> {
                viewModelScope.launch {
                    userPreferences.setExperimentHighlightVariant(event.variant)
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
