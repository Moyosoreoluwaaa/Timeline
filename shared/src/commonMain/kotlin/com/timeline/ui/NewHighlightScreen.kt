package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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

    var showLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3500.milliseconds)
        showLoading = false
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "New Highlight Screen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
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
                    actions = {
                        IconButton(onClick = { showLoading = true; viewModel.onEvent(NewHighlightEvent.TriggerRefresh) }) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = "Refresh Loading State",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Dimensions.IconSmall)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                // Filter chips below top bar
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
                NewHighlightLoadingState(
                    leftThumbnails = state.sampleThumbnailsLeft,
                    rightThumbnails = state.sampleThumbnailsRight
                )
            }

            AnimatedVisibility(
                visible = !showLoading,
                enter = fadeIn(animationSpec = tween(600)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                NewHighlightContentState(text = state.narrativeText)
            }
        }
    }
}

@Composable
private fun NewHighlightLoadingState(
    leftThumbnails: List<String>,
    rightThumbnails: List<String>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_transfer")
    val translationX by infiniteTransition.animateFloat(
        initialValue = 120f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "transfer_indicator"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left stack card
            Card(
                modifier = Modifier
                    .width(130.dp)
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Raw Captures", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    leftThumbnails.forEach { path ->
                        ScreenshotImage(
                            path = path,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            // Animated transfer indicator moving back and forth
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { this.translationX = translationX }
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text("Reasoning...", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Right stack card
            Card(
                modifier = Modifier
                    .width(130.dp)
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("AI Synthesis", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    rightThumbnails.forEach { path ->
                        ScreenshotImage(
                            path = path,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
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
