package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.ui.components.FlatCircularCardsSpinner
import com.timeline.ui.components.bottomFadeMask
import com.timeline.ui.components.spinnerGradientColors
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun NewHighlightLoadingScreen(
    screenshots: List<String>,
    onFinished: () -> Unit = {}
) {
    // 8-second loading timeout for a more immersive "upgrade" feel
    LaunchedEffect(Unit) {
        delay(8000.milliseconds)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Container for the rotating cards with the bottom fade mask applied
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .offset(y = (-20).dp) // Adjust position to balance the screen
                    .bottomFadeMask(),
                contentAlignment = Alignment.Center
            ) {
                FlatCircularCardsSpinner(
                    itemCount = 10,
                    cardSize = 72.dp,
                    radius = 120.dp,
                    rotationDurationMs = 20000
                ) { index ->
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (screenshots.isNotEmpty()) {
                                Color.Transparent
                            } else {
                                spinnerGradientColors[index % spinnerGradientColors.size]
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (screenshots.isNotEmpty()) {
                                ScreenshotImage(
                                    path = screenshots[index % screenshots.size],
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(18.dp))
                                )
                            } else {
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
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Animated Text preserved from the previous loading logic
            FadingWordText(
                text = "Your daily narrative got a major upgrade",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                delayBetweenWords = 130L,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 12.dp)
            )

            FadingWordText(
                text = "Higher-quality reasoning, faster generation, and smarter creative insights",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                delayBetweenWords = 100L,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
