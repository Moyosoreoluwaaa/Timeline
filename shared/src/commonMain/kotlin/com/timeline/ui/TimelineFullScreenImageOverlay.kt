package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget

@Composable
internal fun TimelineFullScreenImageOverlay(
    imagePath: String,
    onDismiss: () -> Unit,
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        ScreenshotImage(
            path = imagePath,
            contentDescription = "Full screen screenshot preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .spotlightTarget(
                    TutorialStep.FULL_SCREEN_IMAGE_PREVIEW,
                    onBoundsCalculated
                )
        )
    }
}
