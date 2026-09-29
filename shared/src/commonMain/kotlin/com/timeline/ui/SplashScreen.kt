package com.timeline.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.timeline_splash_icon

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val sweepProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .drawWithContent {
                    drawContent()
                    val width = size.width
                    val height = size.height
                    val currentX = width * sweepProgress.value * 2f - width

                    val brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        start = Offset(currentX - 100f, 0f),
                        end = Offset(currentX + 100f, height)
                    )
                    drawRect(brush = brush)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.timeline_splash_icon),
                contentDescription = "Splash Logo",
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxSize(1f)
            )
        }
    }
}
