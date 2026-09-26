package com.timeline.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun TutorialShowcaseOverlay(
    state: TutorialState,
    onEvent: (TutorialEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Arrow")
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ArrowOffset"
    )

    AnimatedVisibility(
        visible = state.isActive,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .zIndex(200f)
    ) {
        val activeBounds = state.activeTargetBounds
        val currentStep = state.currentStep

        if (currentStep == TutorialStep.PRO_PAYWALL_STEP || state.isHighlightLoading) {
            // Paywall tutorial step or when Highlight is loading: 100% transparent scrim, no overlay card, allowing user/system to interact without skipping highlight
            return@AnimatedVisibility
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onEvent(TutorialEvent.NextStep)
                }
        ) {
            // Cutout Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            ) {
                // Dimmed background scrim
                drawRect(color = Color.Black.copy(alpha = 0.75f))

                // Clear cutout over active target bounds
                if (activeBounds != null) {
                    val padding = 12.dp.toPx()
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(
                            x = (activeBounds.left - padding).coerceAtLeast(0f),
                            y = (activeBounds.top - padding).coerceAtLeast(0f)
                        ),
                        size = Size(
                            width = activeBounds.width + (padding * 2),
                            height = activeBounds.height + (padding * 2)
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
            }

            // Arrow hints for gestures
            if (currentStep.name.contains("GESTURE", ignoreCase = true)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp + arrowOffset.dp)
                        .graphicsLayer(scaleX = 2f, scaleY = 2f),
                    tint = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp + arrowOffset.dp)
                        .graphicsLayer(scaleX = 2f, scaleY = 2f),
                    tint = Color.White
                )
            }

            // Tooltip Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(
                        if (activeBounds != null && activeBounds.top > 400) Alignment.TopCenter
                        else Alignment.BottomCenter
                    )
                    .padding(vertical = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onEvent(TutorialEvent.NextStep)
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentStep.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep == TutorialStep.COMPLETED) {
                            Button(
                                onClick = { onEvent(TutorialEvent.NextStep) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onEvent(TutorialEvent.SkipTutorial) }
                            ) {
                                Text("Skip")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { onEvent(TutorialEvent.NextStep) }
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }
}
