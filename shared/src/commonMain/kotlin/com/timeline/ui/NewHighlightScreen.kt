package com.timeline.ui

import androidx.compose.runtime.Composable
import com.timeline.presentation.SimplifiedHighlightViewModel
import com.timeline.tutorial.TutorialViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Backwards-compatible alias for SimplifiedHighlightScreen.
 */
@Composable
fun NewHighlightScreen(
    viewModel: SimplifiedHighlightViewModel = koinViewModel(),
    tutorialViewModel: TutorialViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    SimplifiedHighlightScreen(
        viewModel = viewModel,
        tutorialViewModel = tutorialViewModel,
        onNavigateBack = onNavigateBack
    )
}
