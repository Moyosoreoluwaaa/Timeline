package com.timeline.tutorial

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.Session
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TutorialViewModel(
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

            val mockSessions = createMockSessions()

            _state.update {
                it.copy(
                    isPreparingData = false,
                    tutorialSessions = mockSessions,
                    selectedSessionId = mockSessions.firstOrNull()?.id,
                    currentStep = TutorialStep.SPOTLIGHT_APP_ENTRY
                )
            }
        }
    }

    private fun createMockSessions(): List<Session> {
        val now = kotlin.time.Clock.System.now()
        val nowMillis = now.toEpochMilliseconds()

        val session1 = Session(
            id = "tutorial_session_youtube",
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            startTime = now.minus(kotlin.time.Duration.parse("1h")),
            endTime = now.minus(kotlin.time.Duration.parse("45m")),
            durationMinutes = 15,
            durationSeconds = 0,
            screenshots = listOf("mock_youtube_1.jpg"),
            segments = listOf(
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("1h")),
                    screenshotPath = "mock_youtube_1.jpg",
                    activityDescription = "Browsing Subscriptions"
                )
            )
        )

        val session2 = Session(
            id = "tutorial_session_timeline_records_middle",
            packageName = "com.timeline_records",
            displayName = "Timeline Records",
            startTime = now.minus(kotlin.time.Duration.parse("40m")),
            endTime = now.minus(kotlin.time.Duration.parse("15m")),
            durationMinutes = 25,
            durationSeconds = 0,
            screenshots = listOf("mock_timeline_records1.jpg", "mock_timeline_records2.jpg"),
            segments = listOf(
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("40m")),
                    screenshotPath = "mock_timeline_records1.jpg",
                    activityDescription = "Watching Android Dev Tutorial"
                ),
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("25m")),
                    screenshotPath = "mock_timeline_records2.jpg",
                    activityDescription = "Using Timeline"
                )
            )
        )

        val session3 = Session(
            id = "tutorial_session_x",
            packageName = "com.twitter.android",
            displayName = "X",
            startTime = now.minus(kotlin.time.Duration.parse("10m")),
            endTime = now,
            durationMinutes = 10,
            durationSeconds = 0,
            screenshots = listOf("mock_x_1.jpg"),
            segments = listOf(
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("10m")),
                    screenshotPath = "mock_x_1.jpg",
                    activityDescription = "Scrolling timeline"
                )
            )
        )

        return listOf(session1, session2, session3)
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
                // Dismiss sheet and transition to Clock Icon spotlight
                _effects.trySend(TutorialEffect.SetSheetExpanded(false))
                TutorialStep.SPOTLIGHT_TIME_FILTER_ICON
            }

            TutorialStep.SPOTLIGHT_TIME_FILTER_ICON -> {
                // Expanding the time filter menu for the filter section step
                TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION
            }

            TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION -> TutorialStep.SPOTLIGHT_DATE_CONTAINER

            TutorialStep.SPOTLIGHT_DATE_CONTAINER -> TutorialStep.SPOTLIGHT_DATE_PICKER

            TutorialStep.SPOTLIGHT_DATE_PICKER -> TutorialStep.SPOTLIGHT_SETTINGS_ICON

            TutorialStep.SPOTLIGHT_SETTINGS_ICON -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.SETTINGS))
                TutorialStep.SPOTLIGHT_HIGHLIGHTS_PREFERENCE
            }

            TutorialStep.SPOTLIGHT_HIGHLIGHTS_PREFERENCE -> TutorialStep.SPOTLIGHT_REASONING_SHEET
            TutorialStep.SPOTLIGHT_REASONING_SHEET -> TutorialStep.SPOTLIGHT_TRACKING_OPTIONS
            TutorialStep.SPOTLIGHT_TRACKING_OPTIONS -> TutorialStep.SPOTLIGHT_APP_EXCLUSIONS
            TutorialStep.SPOTLIGHT_APP_EXCLUSIONS -> TutorialStep.SPOTLIGHT_EXCLUSIONS_SHEET
            TutorialStep.SPOTLIGHT_EXCLUSIONS_SHEET -> TutorialStep.SPOTLIGHT_DATA_RETENTION
            TutorialStep.SPOTLIGHT_DATA_RETENTION -> TutorialStep.SPOTLIGHT_RETENTION_SHEET

            TutorialStep.SPOTLIGHT_RETENTION_SHEET -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.TIMELINE))
                TutorialStep.SPOTLIGHT_SUMMARY_BAR
            }

            TutorialStep.SPOTLIGHT_SUMMARY_BAR -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.HIGHLIGHT))
                TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD
            }

            TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.TIMELINE))
                TutorialStep.COMPLETED
            }

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
            _state.update { it.copy(isActive = false, tutorialSessions = emptyList<Session>()) }
            userPreferences.setTutorialCompleted(true)
        }
    }
}