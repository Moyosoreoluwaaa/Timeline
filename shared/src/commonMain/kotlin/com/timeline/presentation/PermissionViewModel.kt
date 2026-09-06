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
        viewModelScope.launch {
            userPreferences.state.collect { prefs ->
                val stepName = prefs.lastOnboardingStep
                val savedStep = try {
                    OnboardingStep.valueOf(stepName)
                } catch (_: Exception) {
                    null
                }
                if (savedStep != null && _state.value.currentStep == OnboardingStep.Welcome && savedStep != OnboardingStep.Welcome) {
                    val resolvedStep = resolveInitialStep(savedStep)
                    _state.update { it.copy(currentStep = resolvedStep) }
                }
            }
        }
    }

    private fun resolveInitialStep(savedStep: OnboardingStep): OnboardingStep {
        // If the saved step is for a permission that is already granted, skip directly forward
        return when (savedStep) {
            OnboardingStep.AccessibilityIntro, OnboardingStep.AccessibilityGrant, OnboardingStep.AccessibilitySuccess, OnboardingStep.AccessibilityFailure -> {
                if (permissionManager.hasAccessibilityPermission()) {
                    if (permissionManager.hasUsageStatsPermission()) {
                        if (permissionManager.hasNotificationPermission()) OnboardingStep.AllSet
                        else OnboardingStep.NotificationsIntro
                    } else OnboardingStep.UsageIntro
                } else savedStep
            }
            OnboardingStep.UsageIntro, OnboardingStep.UsageGrant, OnboardingStep.UsageSuccess, OnboardingStep.UsageFailure -> {
                if (permissionManager.hasUsageStatsPermission()) {
                    if (permissionManager.hasNotificationPermission()) OnboardingStep.AllSet
                    else OnboardingStep.NotificationsIntro
                } else savedStep
            }
            OnboardingStep.NotificationsIntro, OnboardingStep.NotificationsGrant -> {
                if (permissionManager.hasNotificationPermission()) OnboardingStep.AllSet
                else savedStep
            }
            else -> savedStep
        }
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
                    userPreferences.setLastOnboardingStep(OnboardingStep.AllSet.name)
                    _effects.send(PermissionEffect.AllGranted)
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
                OnboardingStep.AccessibilityIntro -> {
                    if (permissionManager.hasAccessibilityPermission()) OnboardingStep.AccessibilitySuccess
                    else OnboardingStep.AccessibilityGrant
                }
                OnboardingStep.AccessibilityGrant -> {
                    if (permissionManager.hasAccessibilityPermission()) OnboardingStep.AccessibilitySuccess
                    else OnboardingStep.AccessibilityFailure
                }
                OnboardingStep.AccessibilitySuccess -> OnboardingStep.UsageIntro
                OnboardingStep.AccessibilityFailure -> OnboardingStep.UsageIntro // Flexible fallback: user can skip
                OnboardingStep.UsageIntro -> {
                    if (permissionManager.hasUsageStatsPermission()) OnboardingStep.UsageSuccess
                    else OnboardingStep.UsageGrant
                }
                OnboardingStep.UsageGrant -> {
                    if (permissionManager.hasUsageStatsPermission()) OnboardingStep.UsageSuccess
                    else OnboardingStep.UsageFailure
                }
                OnboardingStep.UsageSuccess -> OnboardingStep.NotificationsIntro
                OnboardingStep.UsageFailure -> OnboardingStep.NotificationsIntro // Flexible fallback
                OnboardingStep.NotificationsIntro -> OnboardingStep.NotificationsGrant
                OnboardingStep.NotificationsGrant -> OnboardingStep.AllSet
                OnboardingStep.AllSet -> OnboardingStep.AllSet
            }
            val newHistory = currentState.stepHistory + currentState.currentStep
            
            viewModelScope.launch {
                userPreferences.setLastOnboardingStep(next.name)
            }

            currentState.copy(currentStep = next, stepHistory = newHistory)
        }
    }

    private fun previousStep() {
        _state.update { currentState ->
            val prev = if (currentState.stepHistory.isNotEmpty()) {
                currentState.stepHistory.last()
            } else {
                when (currentState.currentStep) {
                    OnboardingStep.ValueProp -> OnboardingStep.Welcome
                    OnboardingStep.PermissionOverview -> OnboardingStep.ValueProp
                    OnboardingStep.AccessibilityIntro -> OnboardingStep.PermissionOverview
                    OnboardingStep.AccessibilityGrant -> OnboardingStep.AccessibilityIntro
                    OnboardingStep.AccessibilitySuccess -> OnboardingStep.AccessibilityIntro
                    OnboardingStep.AccessibilityFailure -> OnboardingStep.AccessibilityIntro
                    OnboardingStep.UsageIntro -> OnboardingStep.AccessibilitySuccess
                    OnboardingStep.UsageGrant -> OnboardingStep.UsageIntro
                    OnboardingStep.UsageSuccess -> OnboardingStep.UsageIntro
                    OnboardingStep.UsageFailure -> OnboardingStep.UsageIntro
                    OnboardingStep.NotificationsIntro -> OnboardingStep.UsageSuccess
                    OnboardingStep.NotificationsGrant -> OnboardingStep.NotificationsIntro
                    OnboardingStep.AllSet -> OnboardingStep.NotificationsGrant
                    else -> OnboardingStep.Welcome
                }
            }
            val newHistory = if (currentState.stepHistory.isNotEmpty()) currentState.stepHistory.dropLast(1) else emptyList()
            
            viewModelScope.launch {
                userPreferences.setLastOnboardingStep(prev.name)
            }

            currentState.copy(currentStep = prev, stepHistory = newHistory)
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
