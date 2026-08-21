package com.timeline.presentation

import androidx.lifecycle.ViewModel
import com.timeline.domain.PermissionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class SetupViewModel(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SetupState())
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
            SetupEvent.StartTracking -> {
                _state.update { it.copy(isTrackingStarted = true) }
                _effects.trySend(SetupEffect.AllPermissionsGranted)
            }
        }
    }

    private fun checkPermissions() {
        val hasUsage = permissionManager.hasUsageStatsPermission()
        val hasOverlay = permissionManager.hasOverlayPermission()
        val hasNotification = permissionManager.hasNotificationPermission()
        val hasAccessibility = permissionManager.hasAccessibilityPermission()
        _state.update { it.copy(
            isUsageStatsGranted = hasUsage,
            isOverlayGranted = hasOverlay,
            isNotificationGranted = hasNotification,
            isAccessibilityGranted = hasAccessibility
        ) }
    }
}
