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
    private val userPreferences: UserPreferences,
    private val appInfoProvider: com.timeline.domain.AppInfoProvider,
    private val subscriptionManager: com.timeline.domain.SubscriptionManager
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
            is TutorialEvent.SetHighlightLoading -> _state.update { it.copy(isHighlightLoading = event.isLoading) }
            is TutorialEvent.UpdateTargetBounds -> updateTargetBounds(event.step, event.bounds)
            is TutorialEvent.RealGestureObserved -> handleRealGesture(event.gesture)
        }
    }

    // Applies the "what should the real UI look like for this step" values
    // whenever currentStep changes. This is the ONLY place that decides
    // sheet lock / full-screen state -- TimelineScreen just reads it and
    // enforces sheetLock via confirmValueChange, it never decides
    // independently while the tutorial is active.
    private fun applyRequiredStateFor(step: TutorialStep) {
        val sheetLock: SheetLock? = when (step) {
            // Sheet must stay collapsed -- this is where the thumbnail row
            // is opaque and spotlight-able. Pinned: no drag allowed.
            TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL -> SheetLock.Fixed(expanded = false)

            // Image is shown in a Dialog above the (collapsed) sheet.
            // Dismissing it returns to the collapsed sheet, never expanded.
            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW -> SheetLock.Fixed(expanded = false)

            // The one step where the sheet is meant to be interacted with:
            // starts collapsed, user may drag it open OR tap Next -- both
            // count as completing this step.
            TutorialStep.EXPAND_BOTTOM_SHEET -> SheetLock.Free(startExpanded = false)

            // Sheet must stay expanded so prev/next controls are visible
            // and spotlight-able. Pinned: no drag allowed back down; a real
            // tap on prev/next is what advances this step (see
            // handleRealGesture), not a drag.
            TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR -> SheetLock.Fixed(expanded = true)

            else -> null // hands-off: sheet not part of this step
        }

        val imagePath: String? = when (step) {
            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW -> "mock_preview.png"
            else -> null
        }

        val lockGestures = when (step) {
            TutorialStep.SPOTLIGHT_TIME_FILTER_ICON,
            TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION,
            TutorialStep.SPOTLIGHT_DATE_CONTAINER,
            TutorialStep.SPOTLIGHT_DATE_PICKER,
            TutorialStep.SPOTLIGHT_SUMMARY_BAR,
            TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL,
            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW,
            TutorialStep.EXPAND_BOTTOM_SHEET,
            TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR -> true
            else -> false
        }

        _state.update {
            it.copy(
                sheetLock = sheetLock,
                requiredFullScreenImagePath = imagePath,
                gestureLockActive = lockGestures
            )
        }
    }

    private fun startTutorial() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isActive = true,
                    isPreparingData = true,
                    isProTutorial = true,
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
            applyRequiredStateFor(TutorialStep.SPOTLIGHT_APP_ENTRY)
        }
    }

    private suspend fun createMockSessions(): List<Session> {
        val now = kotlin.time.Clock.System.now()

        val session1 = Session(
            id = "tutorial_session_youtube",
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            startTime = now.minus(kotlin.time.Duration.parse("1h")),
            endTime = now.minus(kotlin.time.Duration.parse("45m")),
            durationMinutes = 15,
            durationSeconds = 0,
            icon = appInfoProvider.getAppIcon("com.google.android.youtube"),
            screenshots = listOf("mock_youtube_1.jpg"),
            segments = listOf(
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("1h")),
                    screenshotPath = "mock_youtube_1.jpg",
                    activityDescription = "Browsing Subscriptions"
                )
            )
        )
        val timelineIcon = appInfoProvider.getAppIcon("com.timeline_records")
        val session2 = Session(
            id = "tutorial_session_timeline_middle",
            packageName = "com.timeline_records",
            displayName = "Timeline",
            startTime = now.minus(kotlin.time.Duration.parse("40m")),
            endTime = now.minus(kotlin.time.Duration.parse("15m")),
            durationMinutes = 25,
            durationSeconds = 0,
            icon = timelineIcon,
            screenshots = listOf("mock_timeline1.jpg", "mock_timeline2.jpg"),
            segments = listOf(
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("40m")),
                    screenshotPath = "mock_timeline1.jpg",
                    activityDescription = "Watching Android Dev Tutorial"
                ),
                com.timeline.domain.SessionSegment(
                    timestamp = now.minus(kotlin.time.Duration.parse("25m")),
                    screenshotPath = "mock_timeline2.jpg",
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
            icon = appInfoProvider.getAppIcon("com.twitter.android"),
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

    // Maps a REAL gesture to "does this count as advancing the current step".
    // This is what lets a genuine tap on the thumbnail / sheet / nav buttons
    // move the tutorial forward instead of the tutorial being blind to them.
    private fun handleRealGesture(gesture: TutorialGesture) {
        val current = _state.value.currentStep
        val matches = when (current) {
            TutorialStep.SPOTLIGHT_APP_ENTRY -> gesture == TutorialGesture.TAPPED_SESSION_ENTRY
            TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL -> gesture == TutorialGesture.TAPPED_THUMBNAIL
            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW -> gesture == TutorialGesture.DISMISSED_FULL_SCREEN_IMAGE
            TutorialStep.EXPAND_BOTTOM_SHEET -> gesture == TutorialGesture.EXPANDED_SHEET
            TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR ->
                gesture == TutorialGesture.TAPPED_PREV_SESSION || gesture == TutorialGesture.TAPPED_NEXT_SESSION
            else -> false
        }
        if (matches) {
            advanceStep()
        }
    }

    private fun advanceStep() {
        val current = _state.value.currentStep

        val nextStep = when (current) {
            TutorialStep.PREREQUISITE_CHECK -> TutorialStep.SPOTLIGHT_APP_ENTRY
            TutorialStep.SPOTLIGHT_APP_ENTRY -> TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL
            TutorialStep.SPOTLIGHT_SCREENSHOT_THUMBNAIL -> TutorialStep.FULL_SCREEN_IMAGE_PREVIEW
            TutorialStep.FULL_SCREEN_IMAGE_PREVIEW -> TutorialStep.EXPAND_BOTTOM_SHEET
            TutorialStep.EXPAND_BOTTOM_SHEET -> TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR
            TutorialStep.SPOTLIGHT_SESSION_NAVIGATOR -> TutorialStep.SPOTLIGHT_TIME_FILTER_ICON

            TutorialStep.SPOTLIGHT_TIME_FILTER_ICON -> TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION
            TutorialStep.SPOTLIGHT_TIME_FILTER_SECTION -> TutorialStep.SPOTLIGHT_DATE_CONTAINER
            TutorialStep.SPOTLIGHT_DATE_CONTAINER -> TutorialStep.SPOTLIGHT_DATE_PICKER

            TutorialStep.SPOTLIGHT_DATE_PICKER -> TutorialStep.SPOTLIGHT_SUMMARY_BAR

            TutorialStep.SPOTLIGHT_SUMMARY_BAR -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.HIGHLIGHT))
                TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD
            }

            TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.TIMELINE))
                TutorialStep.SPOTLIGHT_SETTINGS_ICON
            }

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
                TutorialStep.GESTURE_DRAG_SETTINGS
            }

            TutorialStep.GESTURE_DRAG_SETTINGS -> {
                _effects.trySend(TutorialEffect.NavigateToScreen(TutorialScreen.PAYWALL))
                TutorialStep.PRO_PAYWALL_STEP
            }

            TutorialStep.PRO_PAYWALL_STEP -> {
                dismissTutorial()
                return
            }

            TutorialStep.COMPLETED -> {
                dismissTutorial()
                return
            }
        }

        _state.update { it.copy(currentStep = nextStep) }
        applyRequiredStateFor(nextStep)
        _effects.trySend(TutorialEffect.NavigateToScreen(nextStep.screen))
    }

    private fun rewindStep() {
        val steps = TutorialStep.entries
        val currentIndex = _state.value.currentStep.ordinal
        if (currentIndex > 1) {
            val prevStep = steps[currentIndex - 1]
            _state.update { it.copy(currentStep = prevStep) }
            applyRequiredStateFor(prevStep)
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
            userPreferences.setTutorialCompleted(true)
            _state.update {
                it.copy(
                    isActive = false,
                    isPreparingData = false,
                    tutorialSessions = emptyList<Session>(),
                    currentStep = TutorialStep.COMPLETED,
                    sheetLock = null,
                    requiredFullScreenImagePath = null,
                    requiredSelectedSessionId = null,
                    gestureLockActive = false
                )
            }
        }
    }
}