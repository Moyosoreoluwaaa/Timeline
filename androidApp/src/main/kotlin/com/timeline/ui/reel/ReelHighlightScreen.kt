package com.timeline.ui.reel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.ReelHighlightEvent
import com.timeline.presentation.ReelHighlightViewModel
import com.timeline.presentation.ReelPlaybackState
import com.timeline.presentation.TimeOfDayFilter
import com.timeline.ui.components.CustomTopAppBar
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.theme.Dimensions
import org.koin.compose.viewmodel.koinViewModel

/**
 * Modern story reel highlight screen.
 * Displays executive time-of-day cards that smoothly morph into full-screen interactive story reels.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReelHighlightScreen(
    viewModel: ReelHighlightViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Handle back button for Reel playback
    BackHandler(enabled = state.activeReel != null) {
        viewModel.onEvent(ReelHighlightEvent.CloseReel)
    }

    val isReelOpen = state.activeReel != null
    var cachedReelState by remember { mutableStateOf<ReelPlaybackState?>(null) }
    state.activeReel?.let { cachedReelState = it }

    Box(modifier = Modifier.fillMaxSize()) {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = isReelOpen,
                transitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                },
                label = "CardToReelMorphTransition",
                modifier = Modifier.fillMaxSize()
            ) { open ->
                val activeReel = state.activeReel ?: cachedReelState
                if (open && activeReel != null) {
                    StoryReelModal(
                        reelState = activeReel,
                        dateLabel = state.selectedDateLabel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onClose = { viewModel.onEvent(ReelHighlightEvent.CloseReel) },
                        onNext = { viewModel.onEvent(ReelHighlightEvent.NextSlide) },
                        onPrevious = { viewModel.onEvent(ReelHighlightEvent.PreviousSlide) },
                        onToggleTts = { viewModel.onEvent(ReelHighlightEvent.ToggleTts) },
                        onToggleTicketShare = { viewModel.onEvent(ReelHighlightEvent.ToggleTicketShare(it)) },
                        onPauseChanged = { viewModel.onEvent(ReelHighlightEvent.SetReelPaused(it)) }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            CustomTopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = "Highlights",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (state.selectedDateLabel.isNotBlank()) {
                                            Text(
                                                text = state.selectedDateLabel,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                actions = {
                                    IconButton(
                                        onClick = { viewModel.onEvent(ReelHighlightEvent.SyncRealData) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = "Sync",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                scrollBehavior = scrollBehavior
                            )
                        }
                    ) { padding ->
                        val topPadding = (padding.calculateTopPadding() - TopAppBarCutoutRadius).coerceAtLeast(0.dp)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = topPadding)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            PullToRefreshBox(
                                isRefreshing = state.isRefreshing,
                                onRefresh = { viewModel.onEvent(ReelHighlightEvent.Refresh) },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (state.isLoading && state.segments.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // Day timeline progress bar (Morning, Afternoon, Evening, Now)
                                        item {
                                            ReelDayTimelineBar(
                                                currentFilter = state.timeOfDayFilter,
                                                onFilterSelected = { filter ->
                                                    val nextFilter = if (state.timeOfDayFilter == filter) TimeOfDayFilter.ALL else filter
                                                    viewModel.onEvent(ReelHighlightEvent.SetFilter(nextFilter))
                                                }
                                            )
                                        }

                                        // Filtered Segment Cards
                                        val displayedSegments = if (state.timeOfDayFilter == TimeOfDayFilter.ALL) {
                                            state.segments
                                        } else {
                                            state.segments.filter { it.filter == state.timeOfDayFilter }
                                        }

                                        if (displayedSegments.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                                        .padding(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No captures recorded in this period.",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            items(displayedSegments, key = { it.filter.name }) { segment ->
                                                ReelHighlightCard(
                                                    segment = segment,
                                                    sharedTransitionScope = this@SharedTransitionLayout,
                                                    animatedVisibilityScope = this@AnimatedContent,
                                                    onClick = {
                                                        viewModel.onEvent(ReelHighlightEvent.OpenReel(segment.filter))
                                                    }
                                                )
                                            }
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
