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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val combinedThumbnails = (leftThumbnails + rightThumbnails).distinct()

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "arc_reveal"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "loading_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_cards"
    )

    val mockBrushes = remember {
        listOf(
            Brush.linearGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371))),
            Brush.linearGradient(listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))),
            Brush.linearGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
            Brush.linearGradient(listOf(Color(0xFF00c6ff), Color(0xFF0072ff))),
            Brush.linearGradient(listOf(Color(0xFFf857a6), Color(0xFFff5858)))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Arc Layout of 5 thumbnails / screenshots
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (index in 0 until 5) {
                        val path = combinedThumbnails.getOrNull(index)
                        val brush = mockBrushes[index]

                        val targetRotation = when (index) {
                            0 -> -22f
                            1 -> -11f
                            2 -> 0f
                            3 -> 11f
                            4 -> 22f
                            else -> 0f
                        }

                        val targetTranslationY = when (index) {
                            0 -> 42f
                            1 -> 12f
                            2 -> 0f
                            3 -> 12f
                            4 -> 42f
                            else -> 0f
                        }

                        val targetTranslationX = when (index) {
                            0 -> 24f
                            1 -> 8f
                            2 -> 0f
                            3 -> -8f
                            4 -> -24f
                            else -> 0f
                        }

                        // Apply animations based on index to stagger or curve perfectly
                        Card(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .graphicsLayer {
                                    rotationZ = targetRotation * animationProgress
                                    translationY = (targetTranslationY * animationProgress) + floatOffset
                                    translationX = targetTranslationX * animationProgress
                                }
                                .padding(4.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (path != null) {
                                    ScreenshotImage(
                                        path = path,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(18.dp))
                                    )
                                } else {
                                    // High-quality premium gradient placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(brush)
                                            .clip(RoundedCornerShape(18.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Animated title and description (word-by-word fade-in stream)
            FadingWordText(
                text = "Your daily narrative got a major upgrade",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                delayBetweenWords = 130L,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 12.dp)
            )

            FadingWordText(
                text = "Higher-quality reasoning, faster generation, and smarter creative insights",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.7f),
                delayBetweenWords = 100L,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}

@Composable
private fun FadingWordText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight = FontWeight.Normal,
    delayBetweenWords: Long = 120L,
    modifier: Modifier = Modifier
) {
    val words = remember(text) { text.split(" ") }
    var visibleWords by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleWords = 0
        delay(150.milliseconds)
        while (visibleWords < words.size) {
            visibleWords++
            delay(delayBetweenWords.milliseconds)
        }
    }

    val displayedText = remember(visibleWords, words) {
        words.take(visibleWords).joinToString(" ")
    }

    Text(
        text = displayedText,
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
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
