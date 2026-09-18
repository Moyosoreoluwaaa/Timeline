package com.timeline.tutorial

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.spotlightTarget(
    step: TutorialStep,
    onBoundsCalculated: (TutorialStep, androidx.compose.ui.geometry.Rect) -> Unit
): Modifier = this.onGloballyPositioned { coordinates ->
    val bounds = coordinates.boundsInRoot()
    if (bounds.width > 0f && bounds.height > 0f) {
        onBoundsCalculated(step, bounds)
    }
}