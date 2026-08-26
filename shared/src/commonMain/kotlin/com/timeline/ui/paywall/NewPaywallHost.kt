// File: NewPaywallHost.kt
package com.timeline.ui.paywall

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.timeline.ui.theme.TimelineTheme

enum class NewPaywallStyle {
    Classic,        // Uses SpikedSave20SealBadge
    LimitedOffer,   // Uses PointyHexagonBadge
    FeatureGrid,    // Uses Save20PillBadge
    Comparison      // Uses SpikedSave20SealBadge
}

@Composable
fun NewPaywallScreen(
    style: NewPaywallStyle,
    onDismiss: () -> Unit,
    onStartTrial: (isYearly: Boolean) -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    NewPaywallPalette.ProvideNewPaywallColors(darkTheme = darkTheme) {
        when (style) {
            NewPaywallStyle.Classic -> NewPaywallStyleClassic(onDismiss, onStartTrial)
            NewPaywallStyle.LimitedOffer -> NewPaywallStyleLimitedOffer(onDismiss, onStartTrial)
            NewPaywallStyle.FeatureGrid -> NewPaywallStyleFeatureGrid(onDismiss, onStartTrial)
            NewPaywallStyle.Comparison -> NewPaywallStyleComparison(onDismiss, onStartTrial)
        }
    }
}

// ============================================================================
// DARK MODE PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "1. Classic (Spiked Seal) - Dark")
@Composable
private fun ClassicDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallScreen(NewPaywallStyle.Classic, onDismiss = {}, onStartTrial = {}, darkTheme = true)
    }
}

@Preview(showBackground = true, name = "2. Limited Offer (Pointy Hexagon) - Dark")
@Composable
private fun LimitedOfferDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallScreen(NewPaywallStyle.LimitedOffer, onDismiss = {}, onStartTrial = {}, darkTheme = true)
    }
}

@Preview(showBackground = true, name = "3. Feature Grid (Save 20% Pill) - Dark")
@Composable
private fun FeatureGridDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallScreen(NewPaywallStyle.FeatureGrid, onDismiss = {}, onStartTrial = {}, darkTheme = true)
    }
}

@Preview(showBackground = true, name = "4. Comparison (Spiked Seal) - Dark")
@Composable
private fun ComparisonDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallScreen(NewPaywallStyle.Comparison, onDismiss = {}, onStartTrial = {}, darkTheme = true)
    }
}

// ============================================================================
// LIGHT MODE PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "5. Classic (Spiked Seal) - Light")
@Composable
private fun ClassicLightPreview() {
    TimelineTheme(darkTheme = false) {
        NewPaywallScreen(NewPaywallStyle.Classic, onDismiss = {}, onStartTrial = {}, darkTheme = false)
    }
}

@Preview(showBackground = true, name = "6. Limited Offer (Pointy Hexagon) - Light")
@Composable
private fun LimitedOfferLightPreview() {
    TimelineTheme(darkTheme = false) {
        NewPaywallScreen(NewPaywallStyle.LimitedOffer, onDismiss = {}, onStartTrial = {}, darkTheme = false)
    }
}

@Preview(showBackground = true, name = "7. Feature Grid (Save 20% Pill) - Light")
@Composable
private fun FeatureGridLightPreview() {
    TimelineTheme(darkTheme = false) {
        NewPaywallScreen(NewPaywallStyle.FeatureGrid, onDismiss = {}, onStartTrial = {}, darkTheme = false)
    }
}

@Preview(showBackground = true, name = "8. Comparison (Spiked Seal) - Light")
@Composable
private fun ComparisonLightPreview() {
    TimelineTheme(darkTheme = false) {
        NewPaywallScreen(NewPaywallStyle.Comparison, onDismiss = {}, onStartTrial = {}, darkTheme = false)
    }
}