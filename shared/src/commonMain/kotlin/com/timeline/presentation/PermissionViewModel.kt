package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.PermissionManager
import com.timeline.domain.UserPreferences
import com.timeline.domain.NotificationManager
import com.timeline.util.AppStrings
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PermissionViewModel(
    private val permissionManager: PermissionManager,
    private val userPreferences: UserPreferences,
    private val notificationManager: NotificationManager,
    private val logger: Logger
) : ViewModel() {
    private val tagLogger = logger.withTag("PermissionViewModel")
    private val _state = MutableStateFlow(PermissionState())
    val state = _state.asStateFlow()

    private val _effects = Channel<PermissionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        checkPermissions()
    }

    fun onEvent(event: PermissionEvent) {
        when (event) {
            is PermissionEvent.CheckPermissions -> checkPermissions()
            is PermissionEvent.GrantPermission -> grantPermission(event.id)
            is PermissionEvent.StartTracking -> {
                viewModelScope.launch {
                    userPreferences.setPermissionsCompleted(true)
                    if (state.value.allGranted) {
                        _effects.send(PermissionEffect.AllGranted)
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val allPermissions = listOf(
            PermissionItem(
                id = "usage",
                title = AppStrings.PermissionUsageTitle,
                description = AppStrings.PermissionUsageDesc,
                isGranted = permissionManager.hasUsageStatsPermission(),
                illustration = AppStrings.PermissionUsageIllustration
            ),
            PermissionItem(
                id = "overlay",
                title = AppStrings.PermissionOverlayTitle,
                description = AppStrings.PermissionOverlayDesc,
                isGranted = permissionManager.hasOverlayPermission(),
                illustration = AppStrings.PermissionOverlayIllustration
            ),
            PermissionItem(
                id = "notifications",
                title = AppStrings.PermissionNotificationsTitle,
                description = AppStrings.PermissionNotificationsDesc,
                isGranted = permissionManager.hasNotificationPermission(),
                illustration = AppStrings.PermissionNotificationsIllustration
            ),
            PermissionItem(
                id = "accessibility",
                title = AppStrings.PermissionAccessibilityTitle,
                description = AppStrings.PermissionAccessibilityDesc,
                isGranted = permissionManager.hasAccessibilityPermission(),
                illustration = AppStrings.PermissionAccessibilityIllustration
            ),
            PermissionItem(
                id = "battery",
                title = AppStrings.PermissionBatteryTitle,
                description = AppStrings.PermissionBatteryDesc,
                isGranted = permissionManager.isBatteryOptimizationDisabled(),
                illustration = AppStrings.PermissionBatteryIllustration
            )
        )

        // To re-enable removed permissions, add "overlay" or "battery" back to this list
        val activePermissionIds = listOf("usage", "notifications", "accessibility")
        val permissions = allPermissions.filter { it.id in activePermissionIds }

        val allGranted = permissions.all { p -> p.isGranted }

        tagLogger.d { "Checking permissions. All granted: $allGranted" }
        tagLogger.d { "OneSignal notification permission: ${notificationManager.hasPermission()}" }

        _state.update { it.copy(
            permissions = permissions,
            allGranted = allGranted
        ) }
    }

    private fun grantPermission(id: String) {
        when (id) {
            "usage" -> _effects.trySend(PermissionEffect.NavigateToUsageStatsSettings)
            "overlay" -> _effects.trySend(PermissionEffect.NavigateToOverlaySettings)
            "notifications" -> _effects.trySend(PermissionEffect.RequestNotificationPermission)
            "accessibility" -> _effects.trySend(PermissionEffect.NavigateToAccessibilitySettings)
            "battery" -> _effects.trySend(PermissionEffect.NavigateToBatteryOptimizationSettings)
        }
    }
}