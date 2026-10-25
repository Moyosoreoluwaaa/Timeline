package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.UserPreferences
import com.timeline.presentation.SimplifiedHighlightEvent
import com.timeline.presentation.SimplifiedHighlightViewModel
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.ui.components.CustomTopAppBar
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.theme.Dimensions
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Simplified, polished executive highlight screen presenting morning, afternoon,
 * and evening digest cards and high-level summaries for users.
 *
 * Toggle between this and [RawHighlightScreen] via ExperimentFlags.highlightVariant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedHighlightScreen(
    viewModel: SimplifiedHighlightViewModel = koinViewModel(),
    tutorialViewModel: TutorialViewModel = koinViewModel(),
    isPro: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val userPreferences: UserPreferences = koinInject()
    val prefsState by userPreferences.state.collectAsStateWithLifecycle(initialValue = null)
    val digestFrequency = prefsState?.digestFrequency ?: 1

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CustomTopAppBar(
                title = {
                    val yellowAccent = Color(0xFFFFD54F)
                    Column {
                        Text(
                            text = "Highlights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isPro) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Free Plan",
                                style = MaterialTheme.typography.labelMedium.copy(color = yellowAccent, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(Dimensions.IconSmall)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val topPadding = (padding.calculateTopPadding() - TopAppBarCutoutRadius)
            .coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = {
                    viewModel.onEvent(SimplifiedHighlightEvent.Refresh)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        if (state.isLoading && state.segments.isEmpty()) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(androidx.compose.ui.Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            HighlightCardStack(
                                segments = state.segments,
                                activeFilter = state.timeOfDayFilter,
                                digestFrequency = digestFrequency,
                                onSelectFilter = { filter ->
                                    if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD) {
                                        tutorialViewModel.onEvent(TutorialEvent.NextStep)
                                    } else {
                                        viewModel.onEvent(SimplifiedHighlightEvent.SetFilter(filter))
                                    }
                                },
                                tutorialStep = tutorialState.currentStep,
                                onBoundsCalculated = { step, bounds ->
                                    tutorialViewModel.onEvent(
                                        TutorialEvent.UpdateTargetBounds(step, bounds)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
