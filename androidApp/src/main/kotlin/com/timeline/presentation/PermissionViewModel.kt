package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.analytics.Analytics
import com.timeline.analytics.AnalyticsEvents
import com.timeline.analytics.AnalyticsParams
import com.timeline.domain.NotificationManager
import com.timeline.domain.PermissionManager
import com.timeline.domain.UserPreferences
import com.timeline.presentation.OnboardingStep.Intro
import com.timeline.presentation.OnboardingStep.Opening
import com.timeline.presentation.OnboardingStep.PermissionCardStack
import com.timeline.util.AppStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PermissionViewModel(
    private val permissionManager: PermissionManager,
    private val userPreferences: UserPreferences,
    private val notificationManager: NotificationManager,
    private val analytics: Analytics
) : ViewModel() {
    private val _state = MutableStateFlow(PermissionState())
    val state = _state.asStateFlow()

    private val _effects = Channel<PermissionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var lastLoggedStep: Int = -1

    init {
        checkPermissions()
        logOnboardingStep(1)
        viewModelScope.launch {
            userPreferences.state.collect { prefs ->
                val savedStep = when (val stepName = prefs.lastOnboardingStep) {
                    // step names from before the Intro carousel existed
                    "Welcome", "ValueProposition", "Storytelling", "Customization" -> Intro
                    else -> runCatching { OnboardingStep.valueOf(stepName) }.getOrNull()
                }

                val currentPermissions = _state.value.permissions
                val anyGranted = currentPermissions.any { it.isGranted }

                // If user already granted part of permissions or has saved progress beyond Opening, restore to next permission
                if (_state.value.currentStep == Opening) {
                    if (anyGranted || (savedStep != null && savedStep != Opening)) {
                        val firstUngrantedIndex = currentPermissions.indexOfFirst { !it.isGranted }.let {
                            if (it == -1) currentPermissions.size.coerceAtLeast(0) else it
                        }
                        _state.update {
                            it.copy(
                                currentStep = PermissionCardStack,
                                activeCardIndex = firstUngrantedIndex,
                                isRestoredFlow = true
                            )
                        }
                        logOnboardingStep(8)
                    }
                }
            }
        }
    }

    private fun logOnboardingStep(step: Int) {
        val clampedStep = step.coerceIn(1, 6)
        if (clampedStep != lastLoggedStep) {
            lastLoggedStep = clampedStep
            analytics.log(
                AnalyticsEvents.ONBOARDING_SCREEN_VIEW,
                mapOf(AnalyticsParams.STEP to clampedStep)
            )
        }
    }

    private fun normalizePermissionType(id: String): String = when (id) {
        "usage" -> "usage_access"
        "accessibility" -> "accessibility"
        "notifications" -> "notifications"
        "overlay" -> "overlay"
        else -> id
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
        val prevPermissions = _state.value.permissions.associate { it.id to it.isGranted }

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

        permissions.forEach { p ->
            val wasGranted = prevPermissions[p.id] == true
            if (!wasGranted && p.isGranted) {
                analytics.log(
                    AnalyticsEvents.PERMISSION_RESULT,
                    mapOf(
                        AnalyticsParams.TYPE to normalizePermissionType(p.id),
                        AnalyticsParams.RESULT to "granted"
                    )
                )
            }
        }

        val allGranted = permissions.all { p -> p.isGranted }

        val currentState = _state.value
        val currentCard = currentState.permissions.getOrNull(currentState.activeCardIndex)
        val currentWasJustGranted = currentCard != null &&
            prevPermissions[currentCard.id] == false &&
            permissions.find { it.id == currentCard.id }?.isGranted == true

        if (currentState.currentStep == PermissionCardStack && currentWasJustGranted) {
            // Update permissions and allGranted immediately so UI shows it as granted,
            // then wait 350ms so user sees confirmation before card slide down animation triggers.
            _state.update {
                it.copy(
                    permissions = permissions,
                    allGranted = allGranted
                )
            }
            viewModelScope.launch {
                delay(350L.milliseconds)
                nextStep()
            }
        } else {
            var nextCardIndex = currentState.activeCardIndex
            if (currentState.currentStep == PermissionCardStack && prevPermissions.isEmpty()) {
                while (nextCardIndex < permissions.size && permissions[nextCardIndex].isGranted) {
                    nextCardIndex++
                }
            }
            _state.update {
                it.copy(
                    permissions = permissions,
                    allGranted = allGranted,
                    activeCardIndex = nextCardIndex
                )
            }
        }
    }

    private fun nextStep() {
        val s = _state.value
        when (s.currentStep) {
            PermissionCardStack -> {
                val currentItem = s.permissions.getOrNull(s.activeCardIndex)
                // In strict mode, only advance when current permission is granted
                if (currentItem?.isGranted == true) {
                    if (s.activeCardIndex >= s.permissions.size - 1) {
                        if (s.permissions.all { it.isGranted }) {
                            viewModelScope.launch {
                                analytics.log(AnalyticsEvents.ONBOARDING_COMPLETED)
                                userPreferences.setPermissionsCompleted(true)
                                userPreferences.setLastOnboardingStep(OnboardingStep.Done.name)
                                goTo(OnboardingStep.Done)
                            }
                        }
                    } else {
                        _state.update { it.copy(activeCardIndex = it.activeCardIndex + 1) }
                    }
                }
            }
            OnboardingStep.Done -> {
                viewModelScope.launch {
                    _effects.send(PermissionEffect.AllGranted)
                }
            }
            Intro -> {
                if (s.introPage < INTRO_PAGE_COUNT - 1) {
                    val nextPage = s.introPage + 1
                    _state.update { it.copy(introPage = nextPage) }
                    logOnboardingStep(2 + nextPage)
                } else {
                    viewModelScope.launch {
                        userPreferences.setFocusAreas(s.focusAreas)
                        goTo(OnboardingStep.ThreePermissions)
                    }
                }
            }
            OnboardingStep.ThreePermissions -> {
                goTo(PermissionCardStack)
            }
            Opening -> {
                goTo(Intro)
                logOnboardingStep(2)
            }
        }
    }

    private fun previousStep() {
        val s = _state.value
        if (s.currentStep == PermissionCardStack && s.activeCardIndex > 0) {
            _state.update { it.copy(activeCardIndex = it.activeCardIndex - 1) }
            return
        }
        if (s.currentStep == Intro && s.introPage > 0) {
            val prevPage = s.introPage - 1
            _state.update { it.copy(introPage = prevPage) }
            logOnboardingStep(2 + prevPage)
            return
        }
        val prev = s.stepHistory.lastOrNull() ?: return
        _state.update { it.copy(currentStep = prev, stepHistory = it.stepHistory.dropLast(1)) }
        viewModelScope.launch { userPreferences.setLastOnboardingStep(prev.name) }
        logOnboardingStep(
            when (prev) {
                Opening -> 1
                Intro -> 2 + _state.value.introPage
                OnboardingStep.ThreePermissions -> 7
                PermissionCardStack, OnboardingStep.Done -> 8
            }
        )
    }

    private fun goTo(next: OnboardingStep) {
        _state.update { it.copy(currentStep = next, stepHistory = it.stepHistory + it.currentStep) }
        viewModelScope.launch { userPreferences.setLastOnboardingStep(next.name) }
        logOnboardingStep(
            when (next) {
                Opening -> 1
                Intro -> 2 + _state.value.introPage
                OnboardingStep.ThreePermissions -> 7
                PermissionCardStack, OnboardingStep.Done -> 8
            }
        )
    }

    private fun retryCurrentPermission() {
        // No-op in card stack flow
    }

    private fun grantPermission(id: String) {
        val type = normalizePermissionType(id)
        analytics.log(
            AnalyticsEvents.PERMISSION_PROMPT_SHOWN,
            mapOf(AnalyticsParams.TYPE to type)
        )

        when (id) {
            "usage" -> _effects.trySend(PermissionEffect.NavigateToUsageStatsSettings)
            "notifications" -> _effects.trySend(PermissionEffect.RequestNotificationPermission)
            "accessibility" -> _effects.trySend(PermissionEffect.NavigateToAccessibilitySettings)
        }
    }
}