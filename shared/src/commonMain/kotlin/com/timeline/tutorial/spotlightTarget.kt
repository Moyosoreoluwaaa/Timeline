package com.timeline.tutorial

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Reports this composable's bounds as the target for [step], for the
 * tutorial overlay to cut a spotlight around.
 *
 * [isEligible] guards against reporting bounds for an element that is
 * technically laid out (non-zero size) but not actually the thing the user
 * should be looking at right now -- e.g. a row that's fading out via alpha
 * as a bottom sheet expands over it. Pass a lambda that reflects the
 * current "is this really the visible target" condition (read inside the
 * lambda so it re-evaluates on every positioning pass, not captured once).
 *
 * Defaults to always-eligible for target elements with no competing
 * visibility state.
 */
fun Modifier.spotlightTarget(
    step: TutorialStep,
    onBoundsCalculated: (TutorialStep, androidx.compose.ui.geometry.Rect) -> Unit,
    isEligible: () -> Boolean = { true }
): Modifier = this.onGloballyPositioned { coordinates ->
    val bounds = coordinates.boundsInRoot()
    if (bounds.width > 0f && bounds.height > 0f && isEligible()) {
        onBoundsCalculated(step, bounds)
    }
}