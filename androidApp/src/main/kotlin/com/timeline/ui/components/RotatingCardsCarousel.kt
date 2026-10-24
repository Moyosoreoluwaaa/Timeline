package com.timeline.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotatingCardsCarousel(
    itemCount: Int = 6,
    cardWidth: Dp = 120.dp,
    cardHeight: Dp = 160.dp,
    radius: Dp = 140.dp,
    rotationDurationMs: Int = 6000,
    cardContent: @Composable (index: Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CarouselRotation")
    val currentAngleDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleAnimation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val angleStep = 360f / itemCount

        data class CardRenderState(
            val index: Int,
            val xOffset: Float,
            val zOffset: Float,
            val rotationY: Float
        )

        val cardStates = (0 until itemCount).map { index ->
            val cardAngleDegrees = currentAngleDegrees + (index * angleStep)
            val angleRad = cardAngleDegrees * (PI / 180f).toFloat()

            val x = sin(angleRad) * radius.value
            val z = cos(angleRad) * radius.value

            CardRenderState(
                index = index,
                xOffset = x,
                zOffset = z,
                rotationY = cardAngleDegrees
            )
        }.sortedBy { it.zOffset }

        cardStates.forEach { state ->
            val depthRatio = (state.zOffset + radius.value) / (2 * radius.value)
            val scale = 0.75f + (0.25f * depthRatio)
            val alpha = 0.5f + (0.5f * depthRatio)

            Box(
                modifier = Modifier
                    .offset { IntOffset(state.xOffset.dp.roundToPx(), 0) }
                    .zIndex(state.zOffset)
                    .graphicsLayer {
                        rotationY = state.rotationY
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        cameraDistance = 12f * density
                    }
                    .size(width = cardWidth, height = cardHeight)
            ) {
                cardContent(state.index)
            }
        }
    }
}

private val sampleColors = listOf(
    Color(0xFFE57373),
    Color(0xFF81C784),
    Color(0xFF64B5F6),
    Color(0xFFFFB74D),
    Color(0xFFBA68C8),
    Color(0xFF4DD0E1)
)

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun RotatingCardsCarouselPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        RotatingCardsCarousel(
            itemCount = 6,
            cardWidth = 110.dp,
            cardHeight = 150.dp,
            radius = 130.dp,
            rotationDurationMs = 8000
        ) { index ->
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = sampleColors[index % sampleColors.size]
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Card #${index + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}