package com.timeline.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Extension modifier to mask out the bottom portion of a Composable,
 * leaving only the top curve visible.
 */
fun Modifier.bottomFadeMask(): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        // Draw a vertical alpha mask: top is 100% visible, fading to 0% at the bottom half
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black,       // Fully visible top
                    Color.Black,       // Stay visible across the upper arc
                    Color.Transparent  // Smoothly fade out the bottom cards
                ),
                startY = 0f,
                endY = size.height * 0.75f
            ),
            blendMode = BlendMode.DstIn
        )
    }

@Composable
fun FlatCircularCardsSpinner(
    itemCount: Int = 10,
    cardSize: Dp = 70.dp,
    radius: Dp = 200.dp,
    rotationDurationMs: Int = 25000,
    cardContent: @Composable (index: Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlatSpinnerRotation")

    val currentAngleDegrees by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
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

        (0 until itemCount).forEach { index ->
            val angleDegrees = currentAngleDegrees + (index * angleStep)
            val angleRad = angleDegrees * (PI / 180f).toFloat()

            val xOffset = sin(angleRad) * radius.value
            val yOffset = cos(angleRad) * radius.value

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = xOffset.dp.roundToPx(),
                            y = yOffset.dp.roundToPx()
                        )
                    }
                    .size(cardSize)
            ) {
                cardContent(index)
            }
        }
    }
}

val spinnerGradientColors = listOf(
    Color(0xFFE07A5F),
    Color(0xFFF2CC8F),
    Color(0xFF81B29A),
    Color(0xFF3D405B),
    Color(0xFF2A9D8F),
    Color(0xFFE9C46A),
    Color(0xFFF4A261),
    Color(0xFFE76F51),
    Color(0xFF6A0572),
    Color(0xFF10B981)
)

@Composable
fun MajorUpgradeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Container for the rotating cards with the bottom fade mask applied
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp) // Nudge position upward to align top arc perfectly
                .bottomFadeMask(),   // Removes/fades cards moving into the lower half
            contentAlignment = Alignment.Center
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            FlatCircularCardsSpinner(
                itemCount = 10,
                cardSize = 72.dp,
                radius = 210.dp,
                rotationDurationMs = 20000
            ) { index ->
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = spinnerGradientColors[index % spinnerGradientColors.size]
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Center headline and body text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.Center)
                .offset(y = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Image creation got\na major upgrade",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Higher-quality results, faster generation, and\nsmarter creative tools",
                color = Color(0xFF9E9E9E),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MajorUpgradeScreenPreview() {
    MajorUpgradeScreen()
}