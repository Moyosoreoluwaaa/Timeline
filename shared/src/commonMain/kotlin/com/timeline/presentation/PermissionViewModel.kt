package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.PermissionManager
import com.timeline.domain.UserPreferences
import com.timeline.domain.NotificationManager
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
                if (savedStep != null && _state.value.currentStep == OnboardingStep.Welcome && savedStep != OnboardingStep.Welcome) {
                    val resolvedStep = resolveInitialStep(savedStep)
                    _state.update { it.copy(currentStep = resolvedStep) }
                }
            }
        }
    }

    private fun resolveInitialStep(savedStep: OnboardingStep): OnboardingStep {
        return if (savedStep == OnboardingStep.PermissionCardStack) {
            if (permissionManager.hasAccessibilityPermission() && 
                permissionManager.hasUsageStatsPermission() && 
                permissionManager.hasNotificationPermission()) {
                OnboardingStep.ModeSelection
            } else savedStep
        } else savedStep
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
                    userPreferences.setLastOnboardingStep(OnboardingStep.ModeSelection.name)
                    _effects.send(PermissionEffect.AllGranted)
                }
            }
        }
    }

    fun selectProPlan() {
        viewModelScope.launch {
            userPreferences.setPermissionsCompleted(true)
            userPreferences.setLastOnboardingStep(OnboardingStep.ModeSelection.name)
            _effects.send(PermissionEffect.NavigateToPaywall)
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
            // Update active card index based on grants if we are in the stack
            var nextCardIndex = currentState.activeCardIndex
            if (currentState.currentStep == OnboardingStep.PermissionCardStack) {
                // If current card is granted, move to next
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
        _state.update { currentState ->
            val next = when (currentState.currentStep) {
                OnboardingStep.Welcome -> OnboardingStep.PermissionCardStack
                OnboardingStep.PermissionCardStack -> {
                    // If we are at the end of the stack, move to mode selection
                    if (currentState.activeCardIndex >= currentState.permissions.size - 1) {
                        OnboardingStep.ModeSelection
                    } else {
                        return@update currentState.copy(activeCardIndex = currentState.activeCardIndex + 1)
                    }
                }
                OnboardingStep.ModeSelection -> OnboardingStep.ModeSelection
            }
            
            viewModelScope.launch {
                userPreferences.setLastOnboardingStep(next.name)
            }

            val newHistory = currentState.stepHistory + currentState.currentStep
            currentState.copy(currentStep = next, stepHistory = newHistory)
        }
    }

    private fun previousStep() {
        _state.update { currentState ->
            // If in card stack and not on first card, go back one card
            if (currentState.currentStep == OnboardingStep.PermissionCardStack && currentState.activeCardIndex > 0) {
                return@update currentState.copy(activeCardIndex = currentState.activeCardIndex - 1)
            }

            val prev = if (currentState.stepHistory.isNotEmpty()) {
                currentState.stepHistory.last()
            } else {
                when (currentState.currentStep) {
                    OnboardingStep.PermissionCardStack -> OnboardingStep.Welcome
                    OnboardingStep.ModeSelection -> OnboardingStep.PermissionCardStack
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
        // No-op in the new streamlined flow as the card stack handles retries
    }

    private fun grantPermission(id: String) {
        when (id) {
            "usage" -> _effects.trySend(PermissionEffect.NavigateToUsageStatsSettings)
            "notifications" -> _effects.trySend(PermissionEffect.RequestNotificationPermission)
            "accessibility" -> _effects.trySend(PermissionEffect.NavigateToAccessibilitySettings)
        }
    }
}
