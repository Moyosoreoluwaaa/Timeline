package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.timeline.presentation.PermissionEvent

@Composable
internal fun ScreenshotWall(
    page: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AnimatedVisibility(
            visible = page != HIGHLIGHT_PAGE,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(onboardingTween(250)),
            exit = fadeOut(onboardingTween(150))
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationZ = -10f
                            scaleX = 1.3f
                            scaleY = 1.3f
                            alpha = 0.55f
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { col ->
                                WallTile(
                                    highlighted = (row * 4 + col) % 3 == 0,
                                    modifier = Modifier.weight(1f).height(140.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to introPalette.background.copy(alpha = 0.10f),
                                    0.94f to introPalette.background
                                )
                            )
                        )
                )
            }
        }

        ConceptCardHost(
            page = page,
            focusAreas = focusAreas,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .graphicsLayer { rotationZ = if (page == HIGHLIGHT_PAGE) 0f else -2f }
        )
    }
}

@Composable
private fun WallTile(highlighted: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .clip(shape)
            .background(introPalette.shot)
            .border(0.5.dp, introPalette.stroke, shape)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(
                    if (highlighted) introPalette.accent.copy(alpha = 0.45f) else introPalette.ink.copy(
                        alpha = 0.08f
                    )
                )
        )
        Column(Modifier.padding(8.dp)) {
            Box(
                Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
                    .background(introPalette.ink.copy(alpha = 0.10f))
            )
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier.fillMaxWidth(0.6f).height(5.dp).clip(CircleShape)
                    .background(introPalette.ink.copy(alpha = 0.10f))
            )
        }
    }
}
