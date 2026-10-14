package com.timeline.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal fun Modifier.backdropGlow(variant: FanVariant, color: Color, softer: Boolean): Modifier {
    val k = if (softer) 0.55f else 1f
    return when (variant) {
        FanVariant.CenteredFan -> radialGlow(color, alpha = 0.30f * k, centerYFraction = 0.34f)
        FanVariant.CardHand -> radialGlow(color, alpha = 0.38f * k, centerYFraction = 1.0f)
        FanVariant.ScreenshotWall -> this
    }
}

internal fun Modifier.radialGlow(color: Color, alpha: Float, centerYFraction: Float): Modifier =
    drawBehind {
        drawRect(
            Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width / 2f, size.height * centerYFraction),
                radius = size.maxDimension * 0.62f
            )
        )
    }
