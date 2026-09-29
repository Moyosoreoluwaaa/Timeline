package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.NotificationManager
import com.timeline.domain.PermissionManager
import com.timeline.domain.UserPreferences
import com.timeline.presentation.OnboardingStep.Opening
import com.timeline.presentation.OnboardingStep.PermissionCardStack
import com.timeline.presentation.OnboardingStep.Welcome
import com.timeline.util.AppStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PermissionViewModel(
    private val permissionManager: PermissionManager,
    private val userPreferences: UserPreferences,
    private val notificationManager: NotificationManager
) : ViewModel() {
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
                if (savedStep != null && _state.value.currentStep == Opening && savedStep != Opening) {
                    val resolvedStep = resolveInitialStep(savedStep)
                    _state.update { it.copy(currentStep = resolvedStep) }
                }
            }
        }
    }

    private fun resolveInitialStep(savedStep: OnboardingStep): OnboardingStep {
        return savedStep
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
                    userPreferences.setLastOnboardingStep(PermissionCardStack.name)
                    _effects.send(PermissionEffect.AllGranted)
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = listOf(
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

        val allGranted = permissions.all { p -> p.isGranted }

        _state.update { currentState ->
            var nextCardIndex = currentState.activeCardIndex
            if (currentState.currentStep == PermissionCardStack) {
                if (nextCardIndex < permissions.size && permissions[nextCardIndex].isGranted) {
                    nextCardIndex++
                }
            }

            currentState.copy(
                permissions = permissions,
                allGranted = allGranted,
                activeCardIndex = nextCardIndex
            )
        }
    }

    private fun nextStep() {
        val currentState = _state.value
        if (currentState.currentStep == PermissionCardStack) {
            if (currentState.activeCardIndex >= currentState.permissions.size - 1) {
                viewModelScope.launch {
                    userPreferences.setPermissionsCompleted(true)
                    userPreferences.setLastOnboardingStep(PermissionCardStack.name)
                    _effects.send(PermissionEffect.AllGranted)
                }
                return
            } else {
                _state.update { it.copy(activeCardIndex = it.activeCardIndex + 1) }
                return
            }
        }

        _state.update { state ->
            val next = when (state.currentStep) {
                Opening -> Welcome
                Welcome -> PermissionCardStack
                PermissionCardStack -> PermissionCardStack
            }

            viewModelScope.launch {
                userPreferences.setLastOnboardingStep(next.name)
            }

            val newHistory = state.stepHistory + state.currentStep
            state.copy(currentStep = next, stepHistory = newHistory)
        }
    }

    private fun previousStep() {
        _state.update { currentState ->
            if (currentState.currentStep == PermissionCardStack && currentState.activeCardIndex > 0) {
                return@update currentState.copy(activeCardIndex = currentState.activeCardIndex - 1)
            }

            val prev = if (currentState.stepHistory.isNotEmpty()) {
                currentState.stepHistory.last()
            } else {
                when (currentState.currentStep) {
                    PermissionCardStack -> Welcome
                    Welcome -> Opening
                    Opening -> Opening
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
        // No-op in card stack flow
    }

    private fun grantPermission(id: String) {
        when (id) {
            "usage" -> _effects.trySend(PermissionEffect.NavigateToUsageStatsSettings)
            "notifications" -> _effects.trySend(PermissionEffect.RequestNotificationPermission)
            "accessibility" -> _effects.trySend(PermissionEffect.NavigateToAccessibilitySettings)
        }
    }
}