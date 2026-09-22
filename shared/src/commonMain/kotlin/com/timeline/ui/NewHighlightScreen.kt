package com.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.HighlightSegment
import com.timeline.presentation.NewHighlightEvent
import com.timeline.presentation.NewHighlightViewModel
import com.timeline.presentation.TimeOfDayFilter
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.CustomTopAppBar
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.theme.Dimensions
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHighlightScreen(
    viewModel: NewHighlightViewModel = koinViewModel(),
    tutorialViewModel: TutorialViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()

    var showLoading by remember { mutableStateOf(true) }
    var filterChipsExpanded by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    AnimatedContent(
        targetState = showLoading,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "NewHighlightLoadingTransition",
        modifier = Modifier.fillMaxSize()
    ) { loading ->
        if (loading) {
            NewHighlightLoadingScreen(
                screenshots = state.dynamicScreenshots,
                onFinished = { showLoading = false }
            )
        } else {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    CustomTopAppBar(
                        modifier = Modifier.clickable { filterChipsExpanded = !filterChipsExpanded },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Highlights",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
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
                            // Mode selection removed from top bar per requirements
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
                        onRefresh = {
                            viewModel.onEvent(NewHighlightEvent.Refresh)
                            showLoading = true
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                            ) {
                                HighlightCardStack(
                                    segments = state.segments,
                                    activeFilter = state.timeOfDayFilter,
                                    onSelectFilter = { filter ->
                                        if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD) {
                                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                                        } else {
                                            viewModel.onEvent(NewHighlightEvent.SetFilter(filter))
                                        }
                                    },
                                    tutorialStep = tutorialState.currentStep,
                                    onBoundsCalculated = { step, bounds ->
                                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightCardStack(
    segments: List<HighlightSegment>,
    activeFilter: TimeOfDayFilter,
    onSelectFilter: (TimeOfDayFilter) -> Unit,
    tutorialStep: TutorialStep? = null,
    onBoundsCalculated: (TutorialStep, androidx.compose.ui.geometry.Rect) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val defaultOrder = remember {
        listOf(TimeOfDayFilter.MORNING, TimeOfDayFilter.AFTERNOON, TimeOfDayFilter.EVENING)
    }

    val sortedSegments = remember(segments, activeFilter) {
        val active = if (activeFilter == TimeOfDayFilter.ALL) TimeOfDayFilter.EVENING else activeFilter
        val back = defaultOrder.filter { it != active }
        val orderedFilters = back + active
        orderedFilters.mapNotNull { filter -> segments.find { it.filter == filter } }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        sortedSegments.forEachIndexed { stackIndex, segment ->
            val isFrontmost = stackIndex == sortedSegments.lastIndex
            val offsetFromTop by animateDpAsState(
                targetValue = when (stackIndex) {
                    0 -> 0.dp
                    1 -> 56.dp
                    else -> 112.dp
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "CardTopOffset"
            )

            val elevation by animateDpAsState(
                targetValue = ((stackIndex + 1) * 4).dp,
                label = "CardElevation"
            )

            HighlightSegmentCard(
                segment = segment,
                isFrontmost = isFrontmost,
                offsetFromTop = offsetFromTop,
                elevation = elevation,
                onHeaderClick = {
                    onSelectFilter(segment.filter)
                },
                modifier = if (isFrontmost) Modifier.spotlightTarget(
                    TutorialStep.SPOTLIGHT_HIGHLIGHT_CARD,
                    onBoundsCalculated
                ) else Modifier
            )
        }
    }
}

@Composable
private fun HighlightSegmentCard(
    segment: HighlightSegment,
    isFrontmost: Boolean,
    offsetFromTop: Dp,
    elevation: Dp,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, containerColor) = when (segment.filter) {
        TimeOfDayFilter.MORNING -> Triple(
            Icons.Rounded.WbSunny,
            Color(0xFFFFB703),
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
        TimeOfDayFilter.AFTERNOON -> Triple(
            Icons.Rounded.LightMode,
            Color(0xFF2A9D8F),
            MaterialTheme.colorScheme.surfaceContainer
        )
        TimeOfDayFilter.EVENING -> Triple(
            Icons.Rounded.NightsStay,
            Color(0xFF8E94F2),
            MaterialTheme.colorScheme.surfaceContainerLow
        )
        else -> Triple(
            Icons.Rounded.AutoAwesome,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.surfaceContainer
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = offsetFromTop)
            .clickable(onClick = onHeaderClick),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = containerColor,
        shadowElevation = elevation,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isFrontmost) {
                // Header Bar (Peeking Region - Fixed for background cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onHeaderClick)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = segment.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = segment.timeRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = segment.duration,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            } else {
                // Body Content (Full visibility with scrolling header when frontmost)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scrolling Header for the active card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = segment.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = segment.timeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = iconColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = segment.duration,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = iconColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Narrative Text
                    NewHighlightContentState(text = segment.narrative)

                    // Screenshots / Thumbnails Row
                    if (segment.screenshots.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Captured Visuals",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                segment.screenshots.take(3).forEach { path ->
                                    Card(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(140.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        ScreenshotImage(
                                            path = path,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun NewHighlightContentState(text: String) {
    val words = remember(text) { text.split(" ") }
    var visibleWordCount by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleWordCount = 0
        delay(200.milliseconds)
        while (visibleWordCount < words.size) {
            visibleWordCount++
            delay(120.milliseconds)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val displayedText = remember(visibleWordCount, words) {
            words.take(visibleWordCount).joinToString(" ")
        }

        AnimatedVisibility(
            visible = visibleWordCount > 0,
            enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
        ) {
            Text(
                text = displayedText,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
