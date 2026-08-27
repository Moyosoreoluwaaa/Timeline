package com.timeline.ui.paywall

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.PaywallEffect
import com.timeline.presentation.PaywallState
import com.timeline.presentation.PaywallViewModel
import com.timeline.ui.theme.TimelineTheme

enum class NewPaywallStyle {
    Classic,        
    LimitedOffer,   
    FeatureGrid,    
    Comparison      
}

@Composable
fun NewPaywallScreen(
    viewModel: PaywallViewModel,
    style: NewPaywallStyle,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PaywallEffect.PurchaseSuccess -> onPurchaseSuccess()
                is PaywallEffect.PurchaseError -> {
                    // Show error
                }
            }
        }
    }

    NewPaywallPalette.ProvideNewPaywallColors(darkTheme = darkTheme) {
        when (style) {
            NewPaywallStyle.Classic -> NewPaywallStyleClassic(
                state = state,
                onEvent = viewModel::onEvent,
                onDismiss = onDismiss
            )
            NewPaywallStyle.LimitedOffer -> NewPaywallStyleLimitedOffer(
                state = state,
                onEvent = viewModel::onEvent,
                onDismiss = onDismiss
            )
            NewPaywallStyle.FeatureGrid -> NewPaywallStyleFeatureGrid(
                state = state,
                onEvent = viewModel::onEvent,
                onDismiss = onDismiss
            )
            NewPaywallStyle.Comparison -> NewPaywallStyleComparison(
                state = state,
                onEvent = viewModel::onEvent,
                onDismiss = onDismiss
            )
        }
    }
}

// ============================================================================
// DARK MODE PREVIEWS
// ============================================================================

@Preview(name = "1. Classic - Dark")
@Composable
private fun ClassicDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallPalette.ProvideNewPaywallColors(darkTheme = true) {
            NewPaywallStyleClassic(state = PaywallState(), onEvent = {}, onDismiss = {})
        }
    }
}

@Preview(name = "2. Limited Offer - Dark")
@Composable
private fun LimitedOfferDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallPalette.ProvideNewPaywallColors(darkTheme = true) {
            NewPaywallStyleLimitedOffer(state = PaywallState(), onEvent = {}, onDismiss = {})
        }
    }
}

@Preview(name = "3. Feature Grid - Dark")
@Composable
private fun FeatureGridDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallPalette.ProvideNewPaywallColors(darkTheme = true) {
            NewPaywallStyleFeatureGrid(state = PaywallState(), onEvent = {}, onDismiss = {})
        }
    }
}

@Preview(name = "4. Comparison - Dark")
@Composable
private fun ComparisonDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallPalette.ProvideNewPaywallColors(darkTheme = true) {
            NewPaywallStyleComparison(state = PaywallState(), onEvent = {}, onDismiss = {})
        }
    }
}
