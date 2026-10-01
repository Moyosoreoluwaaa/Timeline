package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.NotificationManager
import com.timeline.domain.PermissionManager
import com.timeline.domain.UserPreferences
import com.timeline.presentation.OnboardingStep.Intro
import com.timeline.presentation.OnboardingStep.Opening
import com.timeline.presentation.OnboardingStep.PermissionCardStack
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
                val savedStep = when (val stepName = prefs.lastOnboardingStep) {
                    // step names from before the Intro carousel existed
                    "Welcome", "ValueProposition", "Storytelling", "Customization" -> Intro
                    else -> runCatching { OnboardingStep.valueOf(stepName) }.getOrNull()
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
            is PermissionEvent.SkipIntro -> goTo(PermissionCardStack)
            is PermissionEvent.ToggleFocusArea -> _state.update { s ->
                s.copy(
                    focusAreas = if (event.id in s.focusAreas) s.focusAreas - event.id
                    else s.focusAreas + event.id
                )
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
        val s = _state.value
        when (s.currentStep) {
            PermissionCardStack -> {
                if (s.activeCardIndex >= s.permissions.size - 1) {
                    viewModelScope.launch {
                        userPreferences.setPermissionsCompleted(true)
                        userPreferences.setLastOnboardingStep(PermissionCardStack.name)
                        _effects.send(PermissionEffect.AllGranted)
                    }
                } else {
                    _state.update { it.copy(activeCardIndex = it.activeCardIndex + 1) }
                }
            }
            Intro -> {
                if (s.introPage < INTRO_PAGE_COUNT - 1) {
                    _state.update { it.copy(introPage = it.introPage + 1) }
                } else {
                    // TODO: persist s.focusAreas here (needs a new UserPreferences setter)
                    goTo(PermissionCardStack)
                }
            }
            Opening -> goTo(Intro)
        }
    }

    private fun previousStep() {
        val s = _state.value
        if (s.currentStep == PermissionCardStack && s.activeCardIndex > 0) {
            _state.update { it.copy(activeCardIndex = it.activeCardIndex - 1) }
            return
        }
        if (s.currentStep == Intro && s.introPage > 0) {
            _state.update { it.copy(introPage = it.introPage - 1) }
            return
        }
        val prev = s.stepHistory.lastOrNull() ?: return
        _state.update { it.copy(currentStep = prev, stepHistory = it.stepHistory.dropLast(1)) }
        viewModelScope.launch { userPreferences.setLastOnboardingStep(prev.name) }
    }

    private fun goTo(next: OnboardingStep) {
        _state.update { it.copy(currentStep = next, stepHistory = it.stepHistory + it.currentStep) }
        viewModelScope.launch { userPreferences.setLastOnboardingStep(next.name) }
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