package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.PermissionManager
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(
    private val permissionManager: PermissionManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(
        SetupState(
            isUsageStatsGranted = permissionManager.hasUsageStatsPermission(),
            isOverlayGranted = permissionManager.hasOverlayPermission(),
            isNotificationGranted = permissionManager.hasNotificationPermission(),
            isAccessibilityGranted = permissionManager.hasAccessibilityPermission(),
            isBatteryOptimizationDisabled = permissionManager.isBatteryOptimizationDisabled()
        )
    )
    val state = _state.asStateFlow()

    private val _effects = Channel<SetupEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: SetupEvent) {
        when (event) {
            SetupEvent.CheckPermissions -> checkPermissions()
            SetupEvent.RequestUsageStats -> _effects.trySend(SetupEffect.NavigateToUsageStatsSettings)
            SetupEvent.RequestOverlay -> _effects.trySend(SetupEffect.NavigateToOverlaySettings)
            SetupEvent.RequestNotification -> _effects.trySend(SetupEffect.RequestNotificationPermission)
            SetupEvent.RequestAccessibility -> _effects.trySend(SetupEffect.NavigateToAccessibilitySettings)
            SetupEvent.RequestDisableBatteryOptimization -> _effects.trySend(SetupEffect.NavigateToBatteryOptimizationSettings)
            SetupEvent.StartTracking -> {
                viewModelScope.launch {
                    userPreferences.setSetupCompleted(true)
                    _state.update { it.copy(isTrackingStarted = true) }
                    _effects.trySend(SetupEffect.AllPermissionsGranted)
                }
            }
        }
    }

    private fun checkPermissions() {
        val hasUsage = permissionManager.hasUsageStatsPermission()
        val hasOverlay = permissionManager.hasOverlayPermission()
        val hasNotification = permissionManager.hasNotificationPermission()
        val hasAccessibility = permissionManager.hasAccessibilityPermission()
        val isBatteryOptimized = permissionManager.isBatteryOptimizationDisabled()
        _state.update { it.copy(
            isUsageStatsGranted = hasUsage,
            isOverlayGranted = hasOverlay,
            isNotificationGranted = hasNotification,
            isAccessibilityGranted = hasAccessibility,
            isBatteryOptimizationDisabled = isBatteryOptimized
        ) }
    }
}
