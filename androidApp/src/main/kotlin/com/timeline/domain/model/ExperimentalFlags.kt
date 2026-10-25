package com.timeline.domain.model

import com.timeline.ui.components.FanVariant
import com.timeline.ui.paywall.NewPaywallStyle

/**
 * Highlight synthesis loading animation variants for A/B experimentation.
 */
enum class HighlightLoadingStyle {
    CardStack,
    Arc,
    Stacked
}

/**
 * Highlight screen variant:
 * - [Simplified]: Polished user-facing executive highlights (stacked card UI).
 * - [RawDiagnostic]: Developer-level OCR dumps, PII redaction inspector, and app breakdown.
 * - [ReelHighlight]: Story reel experience — Instagram-style slides for each time window.
 */
enum class HighlightVariant {
    Simplified,
    RawDiagnostic,
    ReelHighlight
}

/**
 * Centralized A/B testing and experimentation configuration flags.
 */
data class ExperimentFlags(
    val onboardingVariant: FanVariant = FanVariant.CenteredFan,
    val paywallStyle: NewPaywallStyle = NewPaywallStyle.Classic,
    val highlightVariant: HighlightVariant = HighlightVariant.ReelHighlight,
    val highlightLoadingStyle: HighlightLoadingStyle = HighlightLoadingStyle.CardStack
)
