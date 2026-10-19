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
import androidx.compose.ui.unit.dp
import com.timeline.util.AppStrings
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

    // Step through the loading messages automatically
    var currentStepIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (currentStepIndex < AppStrings.HighlightLoadingSteps.lastIndex) {
            delay(3500.milliseconds)
            currentStepIndex++
        }
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

    // Shimmer for the skeleton lines below the headline
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
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
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // ── Arc carousel ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val radiusX = 180f
                val radiusY = 48f

                for (i in 0 until itemCount) {
                    val angle = (2 * PI / itemCount * i + rotationPhase).toFloat()
                    val normalizedSin = sin(angle)
                    val normalizedCos = cos(angle)

                    val translationX = normalizedCos * radiusX
                    val translationY = -normalizedSin * radiusY
                    val rotationZ = normalizedCos * 22f
                    val scale = (0.85f + (1f - kotlin.math.abs(normalizedCos)) * 0.2f).coerceIn(0.75f, 1.05f)
                    val alpha = if (normalizedSin >= -0.2f) {
                        (1f - (kotlin.math.abs(normalizedCos) * 0.4f)).coerceIn(0.2f, 1f)
                    } else {
                        0f
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

            Spacer(modifier = Modifier.weight(1f))

            // ── Left-aligned headline step message ────────────────────────
            Text(
                text = AppStrings.HighlightLoadingSteps[currentStepIndex],
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Shimmer skeleton lines ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha))
            )

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}