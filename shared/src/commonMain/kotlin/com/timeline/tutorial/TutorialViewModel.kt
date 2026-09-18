package com.timeline.tutorial

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.data.TimelineRepository
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TutorialViewModel(
    private val repository: TimelineRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(TutorialState())
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    private val _effects = Channel<TutorialEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: TutorialEvent) {
        when (event) {
            TutorialEvent.StartTutorial -> startTutorial()
            TutorialEvent.NextStep -> advanceStep()
            TutorialEvent.PreviousStep -> rewindStep()
            TutorialEvent.SkipTutorial -> dismissTutorial()
            is TutorialEvent.UpdateTargetBounds -> updateTargetBounds(event.step, event.bounds)
        }
    }

    private fun startTutorial() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isActive = true,
                    isPreparingData = true,
                    currentStep = TutorialStep.PREREQUISITE_CHECK
                )
            }

            // Use first() instead of collect so the coroutine unblocks
            val sessions = repository.getTimeline().first()
            if (sessions.isEmpty()) {
                repository.seedMockData()
            }

            _state.update {
                it.copy(
                    isPreparingData = false,
                    currentStep = TutorialStep.SPOTLIGHT_APP_ENTRY
                )
            }
        }
    }

    private fun advanceStep() {
        val current = _state.value.currentStep
        val nextStep = when (current) {
            TutorialStep.PREREQUISITE_CHECK -> TutorialStep.SPOTLIGHT_APP_ENTRY
            TutorialStep.SPOTLIGHT_APP_ENTRY -> {
                _effects.trySend(TutorialEffect.SetSheetExpanded(false))
                TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL
            }

            TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL -> {
                _effects.trySend(TutorialEffect.TriggerFullScreenImage("mock_preview.png"))
                TutorialStep.FULL_SCREEN_IMAGE_PREVIEW
            }

            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW -> {
                _effects.trySend(TutorialEffect.TriggerFullScreenImage(null))
                TutorialStep.EXPAND_BOTTOM_SHEET
            }

            TutorialStep.EXPAND_BOTTOM_SHEET -> {
                _effects.trySend(TutorialEffect.SetSheetExpanded(true))
                TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR
            }

            TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR -> {
                _effects.trySend(TutorialEffect.SetSheetExpanded(false))
                TutorialStep.SPOTLIGHT_SUMMARY_BAR
            }

            TutorialStep.SPOTLIGHT_SUMMARY_BAR -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.HIGHLIGHT))
                TutorialStep.SPOTLIGHT_HIGHLIGHT_METRICS
            }

            TutorialStep.SPOTLIGHT_HIGHLIGHT_METRICS -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.SETTINGS))
                TutorialStep.SPOTLIGHT_MASTER_CAPTURE_TOGGLE
            }

            TutorialStep.SPOTLIGHT_MASTER_CAPTURE_TOGGLE -> TutorialStep.SPOTLIGHT_EXCLUSION_LIST
            TutorialStep.SPOTLIGHT_EXCLUSION_LIST -> TutorialStep.SPOTLIGHT_RETENTION_DURATION
            TutorialStep.SPOTLIGHT_RETENTION_DURATION -> TutorialStep.COMPLETED
            TutorialStep.COMPLETED -> {
                dismissTutorial()
                return
            }
        }

        _state.update { it.copy(currentStep = nextStep) }
        _effects.trySend(TutorialEffect.NavigateToScreen(nextStep.screen))
    }

    private fun rewindStep() {
        val steps = TutorialStep.entries
        val currentIndex = _state.value.currentStep.ordinal
        if (currentIndex > 1) {
            val prevStep = steps[currentIndex - 1]
            _state.update { it.copy(currentStep = prevStep) }
            _effects.trySend(TutorialEffect.NavigateToScreen(prevStep.screen))
        }
    }

    private fun updateTargetBounds(step: TutorialStep, bounds: Rect) {
        _state.update { currentState ->
            val updatedMap = currentState.targetBoundsMap.toMutableMap()
            updatedMap[step] = bounds
            currentState.copy(targetBoundsMap = updatedMap)
        }
    }

    private fun dismissTutorial() {
        viewModelScope.launch {
            _state.update { it.copy(isActive = false) }
            userPreferences.setTutorialCompleted(true)
        }
    }
}