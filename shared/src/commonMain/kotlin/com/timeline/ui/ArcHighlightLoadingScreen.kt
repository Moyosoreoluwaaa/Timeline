package com.timeline.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ArcHighlightLoadingScreen(
    screenshots: List<String>,
    onFinished: () -> Unit = {}
) {
    // 15-second loading timeout
    LaunchedEffect(Unit) {
        delay(15000.milliseconds)
        onFinished()
    }

    // Smooth continuous movement along the arc
    val infiniteTransition = rememberInfiniteTransition(label = "arc_rotation")
    val rotationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val mockBrushes = remember {
        listOf(
            Brush.linearGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371))),
            Brush.linearGradient(listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))),
            Brush.linearGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
            Brush.linearGradient(listOf(Color(0xFF00c6ff), Color(0xFF0072ff))),
            Brush.linearGradient(listOf(Color(0xFFf857a6), Color(0xFFff5858))),
            Brush.linearGradient(listOf(Color(0xFF43e97b), Color(0xFF38f9d7)))
        )
    }

    val itemCount = maxOf(screenshots.size, 6)

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
            // Arc Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dimensions tuned for horizontal arc curve
                val radiusX = 160f
                val radiusY = 40f

                for (i in 0 until itemCount) {
                    val angle = (2 * PI / itemCount * i + rotationPhase).toFloat()
                    val normalizedSin = sin(angle)
                    val normalizedCos = cos(angle)

                    // Curve layout calculation
                    val translationX = normalizedCos * radiusX
                    val translationY = -normalizedSin * radiusY // Negative value arches center upwards

                    // Inward rotation along the curve
                    val rotationZ = normalizedCos * 22f

                    // Subtle scale drop toward outer edges
                    val scale = (0.85f + (1f - kotlin.math.abs(normalizedCos)) * 0.2f).coerceIn(0.75f, 1.05f)

                    // Smooth fade out for cards rotating toward the back/off-screen
                    val alpha = if (normalizedSin >= -0.2f) {
                        (1f - (kotlin.math.abs(normalizedCos) * 0.4f)).coerceIn(0.2f, 1f)
                    } else {
                        0f // Hide back-facing cards to maintain single arc curve
                    }

                    val path = if (screenshots.isNotEmpty()) screenshots[i % screenshots.size] else null
                    val brush = mockBrushes[i % mockBrushes.size]

                    if (alpha > 0f) {
                        Card(
                            modifier = Modifier
                                .size(width = 90.dp, height = 110.dp)
                                .graphicsLayer {
                                    this.translationX = translationX
                                    this.translationY = translationY
                                    this.rotationZ = rotationZ
                                    this.scaleX = scale
                                    this.scaleY = scale
                                    this.alpha = alpha
                                }
                                .padding(4.dp),
                            shape = RoundedCornerShape(20.dp),
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
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(brush)
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Animated Text
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
fun FadingWordText(
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
        textAlign = TextAlign.Center
    )
}