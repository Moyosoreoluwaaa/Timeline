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
            is PermissionEvent.NextStep -> nextStep()
            is PermissionEvent.PreviousStep -> previousStep()
            is PermissionEvent.RetryPermission -> retryCurrentPermission()
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
                id = "accessibility",
                title = AppStrings.PermissionAccessibilityTitle,
                description = AppStrings.PermissionAccessibilityDesc,
                isGranted = permissionManager.hasAccessibilityPermission(),
                illustration = AppStrings.PermissionAccessibilityIllustration
            ),
            PermissionItem(
                id = "usage",
                title = AppStrings.PermissionUsageTitle,
                description = AppStrings.PermissionUsageDesc,
                isGranted = permissionManager.hasUsageStatsPermission(),
                illustration = AppStrings.PermissionUsageIllustration
            ),
            PermissionItem(
                id = "notifications",
                title = AppStrings.PermissionNotificationsTitle,
                description = AppStrings.PermissionNotificationsDesc,
                isGranted = permissionManager.hasNotificationPermission(),
                illustration = AppStrings.PermissionNotificationsIllustration
            )
        )

        val permissions = allPermissions
        val allGranted = permissions.all { p -> p.isGranted }

        tagLogger.d { "Checking permissions. All granted: $allGranted" }

        _state.update { currentState ->
            val newState = currentState.copy(
                permissions = permissions,
                allGranted = allGranted
            )
            
            // Auto-advance logic based on permission status changes
            when (newState.currentStep) {
                OnboardingStep.AccessibilityGrant, OnboardingStep.AccessibilityFailure -> {
                    if (permissionManager.hasAccessibilityPermission()) {
                        newState.copy(currentStep = OnboardingStep.AccessibilitySuccess)
                    } else newState
                }
                OnboardingStep.UsageGrant, OnboardingStep.UsageFailure -> {
                    if (permissionManager.hasUsageStatsPermission()) {
                        newState.copy(currentStep = OnboardingStep.UsageSuccess)
                    } else newState
                }
                OnboardingStep.NotificationsGrant -> {
                    if (permissionManager.hasNotificationPermission()) {
                        newState.copy(currentStep = OnboardingStep.AllSet)
                    } else newState
                }
                else -> newState
            }
        }
    }

    private fun nextStep() {
        _state.update { currentState ->
            val next = when (currentState.currentStep) {
                OnboardingStep.Welcome -> OnboardingStep.ValueProp
                OnboardingStep.ValueProp -> OnboardingStep.PermissionOverview
                OnboardingStep.PermissionOverview -> OnboardingStep.AccessibilityIntro
                OnboardingStep.AccessibilityIntro -> OnboardingStep.AccessibilityGrant
                OnboardingStep.AccessibilitySuccess -> OnboardingStep.UsageIntro
                OnboardingStep.AccessibilityFailure -> OnboardingStep.AccessibilityGrant
                OnboardingStep.UsageIntro -> OnboardingStep.UsageGrant
                OnboardingStep.UsageSuccess -> OnboardingStep.NotificationsIntro
                OnboardingStep.UsageFailure -> OnboardingStep.UsageGrant
                OnboardingStep.NotificationsIntro -> OnboardingStep.NotificationsGrant
                OnboardingStep.NotificationsGrant -> OnboardingStep.AllSet
                OnboardingStep.AllSet -> OnboardingStep.AllSet
                else -> currentState.currentStep
            }
            currentState.copy(currentStep = next)
        }
    }

    private fun previousStep() {
        _state.update { currentState ->
            val prev = when (currentState.currentStep) {
                OnboardingStep.ValueProp -> OnboardingStep.Welcome
                OnboardingStep.PermissionOverview -> OnboardingStep.ValueProp
                OnboardingStep.AccessibilityIntro -> OnboardingStep.PermissionOverview
                OnboardingStep.UsageIntro -> OnboardingStep.AccessibilitySuccess
                OnboardingStep.NotificationsIntro -> OnboardingStep.UsageSuccess
                else -> currentState.currentStep
            }
            currentState.copy(currentStep = prev)
        }
    }

    private fun retryCurrentPermission() {
        val currentStep = state.value.currentStep
        val permissionId = when (currentStep) {
            OnboardingStep.AccessibilityFailure -> "accessibility"
            OnboardingStep.UsageFailure -> "usage"
            else -> null
        }
        permissionId?.let { grantPermission(it) }
    }

    private fun grantPermission(id: String) {
        when (id) {
            "usage" -> _effects.trySend(PermissionEffect.NavigateToUsageStatsSettings)
            "notifications" -> _effects.trySend(PermissionEffect.RequestNotificationPermission)
            "accessibility" -> _effects.trySend(PermissionEffect.NavigateToAccessibilitySettings)
        }
    }
}
