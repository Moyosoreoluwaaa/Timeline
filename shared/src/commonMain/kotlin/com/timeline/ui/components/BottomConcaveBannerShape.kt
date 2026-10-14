package com.timeline.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

val TopAppBarCutoutRadius: Dp = 24.dp

/**
 * A custom Shape that creates a concave cutout along the bottom edge,
 * curving upward into the corners and providing a safe baseline.
 */
class BottomConcaveBannerShape(
    private val cutoutRadius: Dp = TopAppBarCutoutRadius
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cutoutRadius.toPx() }.coerceAtMost(size.height / 2f)

        val path = Path().apply {
            reset()
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)

            // Bottom-right concave arc
            quadraticTo(
                size.width,
                size.height - radiusPx,
                size.width - radiusPx,
                size.height - radiusPx
            )

            // Flat baseline where content rests safely above cutout
            lineTo(radiusPx, size.height - radiusPx)

            // Bottom-left concave arc
            quadraticTo(
                0f,
                size.height - radiusPx,
                0f,
                size.height
            )

            close()
        }

        return Outline.Generic(path)
    }
}

/** Typealias for ergonomic reference across any banner or surface in the app. */
typealias BottomConcaveShape = BottomConcaveBannerShape