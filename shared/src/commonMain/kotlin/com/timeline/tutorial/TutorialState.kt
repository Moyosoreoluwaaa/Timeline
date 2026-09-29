package com.timeline.tutorial

import androidx.compose.ui.geometry.Rect
import com.timeline.domain.Session

data class TutorialState(
    val currentStep: TutorialStep = TutorialStep.PREREQUISITE_CHECK,
    val isActive: Boolean = false,
    val isPreparingData: Boolean = false,
    val isProTutorial: Boolean = true,
    val isHighlightLoading: Boolean = false,
    val tutorialSessions: List<Session> = emptyList(),
    val selectedSessionId: String? = null,
    val targetBoundsMap: Map<TutorialStep, Rect> = emptyMap(),
    // Single source of truth for sheet/full-screen state while a tutorial is
    // active. TimelineScreen reads these instead of maintaining its own
    // independent notion of "should the sheet be expanded right now", AND
    // uses sheetLock to veto real drags that would leave the step's allowed
    // state(s) -- not just to react after the fact.
    val sheetLock: SheetLock? = null,
    val requiredFullScreenImagePath: String? = null,
    val requiredSelectedSessionId: String? = null,
    // While true, real taps that would contradict the current step's
    // required state (e.g. opening a different session) are ignored
    // rather than fought after the fact.
    val gestureLockActive: Boolean = false
) {
    val activeTargetBounds: Rect?
        get() = targetBoundsMap[currentStep]
}

// Describes what the bottom sheet is allowed to do during a tutorial step.
// null (no SheetLock at all) means the tutorial isn't constraining the sheet
// this step -- real UI behaves normally.
sealed interface SheetLock {
    // Sheet is pinned at exactly this value. confirmValueChange in
    // TimelineScreen rejects any drag attempt that would move it elsewhere,
    // so a real drag physically cannot desync it from what the tutorial
    // wants -- there's nothing to "fight back" after the fact.
    data class Fixed(val expanded: Boolean) : SheetLock

    // Sheet starts at [startExpanded] but the user may drag it to the other
    // value; reaching that other value is real user progress and should be
    // reported as a gesture. Used for exactly one step: EXPAND_BOTTOM_SHEET.
    data class Free(val startExpanded: Boolean) : SheetLock
}

sealed interface TutorialEvent {
    data object StartTutorial : TutorialEvent
    data object NextStep : TutorialEvent
    data object PreviousStep : TutorialEvent
    data object SkipTutorial : TutorialEvent
    data class SetHighlightLoading(val isLoading: Boolean) : TutorialEvent
    data class UpdateTargetBounds(val step: TutorialStep, val bounds: Rect) : TutorialEvent
    // Reported by TimelineScreen/TimelineViewModel when a REAL user gesture
    // happens that the active tutorial step cares about. The tutorial
    // decides whether this counts as progress; it is never ignored silently.
    data class RealGestureObserved(val gesture: TutorialGesture) : TutorialEvent
}

// Real, user-driven gestures the tutorial can react to. Kept separate from
// TutorialStep so the mapping of "which gesture advances which step" lives
// in one place (TutorialViewModel) instead of being inferred from screen code.
enum class TutorialGesture {
    TAPPED_SESSION_ENTRY,
    TAPPED_THUMBNAIL,
    DISMISSED_FULL_SCREEN_IMAGE,
    EXPANDED_SHEET,
    COLLAPSED_SHEET,
    TAPPED_PREV_SESSION,
    TAPPED_NEXT_SESSION
}

sealed interface TutorialEffect {
    data class NavigateToScreen(val screen: TutorialScreen) : TutorialEffect
}