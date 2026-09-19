package com.timeline.tutorial

import androidx.compose.ui.geometry.Rect
import com.timeline.domain.Session

data class TutorialState(
    val currentStep: TutorialStep = TutorialStep.PREREQUISITE_CHECK,
    val isActive: Boolean = false,
    val isPreparingData: Boolean = false,
    val tutorialSessions: List<Session> = emptyList(),
    val selectedSessionId: String? = null,
    val targetBoundsMap: Map<TutorialStep, Rect> = emptyMap()
) {
    val activeTargetBounds: Rect?
        get() = targetBoundsMap[currentStep]
}
sealed interface TutorialEvent {
    data object StartTutorial : TutorialEvent
    data object NextStep : TutorialEvent
    data object PreviousStep : TutorialEvent
    data object SkipTutorial : TutorialEvent
    data class UpdateTargetBounds(val step: TutorialStep, val bounds: Rect) : TutorialEvent
}

sealed interface TutorialEffect {
    data class NavigateToScreen(val screen: TutorialScreen) : TutorialEffect
    data class SetSheetExpanded(val expanded: Boolean) : TutorialEffect
    data class TriggerFullScreenImage(val path: String?) : TutorialEffect
}