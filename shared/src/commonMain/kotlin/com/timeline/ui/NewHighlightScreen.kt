package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.NewHighlightEvent
import com.timeline.presentation.NewHighlightViewModel
import com.timeline.presentation.TimeOfDayFilter
import com.timeline.ui.theme.Dimensions
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHighlightScreen(
    viewModel: NewHighlightViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 5-second loading animation
    var showLoading by remember { mutableStateOf(true) }
    // Toggle whether filter chips are expanded below top bar
    var filterChipsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable { filterChipsExpanded = !filterChipsExpanded }
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "New Highlight Screen",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (filterChipsExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (filterChipsExpanded) "Collapse filters" else "Expand filters",
                                modifier = Modifier.size(Dimensions.IconSmall)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(Dimensions.IconSmall)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                // Expandable / Collapsible filter chips below top bar
                AnimatedVisibility(
                    visible = filterChipsExpanded,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)
                    ) {
                        items(TimeOfDayFilter.entries.toTypedArray()) { filter ->
                            FilterChip(
                                selected = state.timeOfDayFilter == filter,
                                onClick = { viewModel.onEvent(NewHighlightEvent.SetFilter(filter)) },
                                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AnimatedVisibility(
                visible = showLoading,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                ArcHighlightLoadingScreen(
                    screenshots = state.dynamicScreenshots,
                    onFinished = { showLoading = false }
                )
            }

            AnimatedVisibility(
                visible = !showLoading,
                enter = fadeIn(animationSpec = tween(600)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        viewModel.onEvent(NewHighlightEvent.Refresh)
                        showLoading = true
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    NewHighlightContentState(text = state.narrativeText)
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
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
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
