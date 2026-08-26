// File: NewPaywallShapes.kt
package com.timeline.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Universal polygon and starburst generator with smoothed quadratic corners.
 */
class NewPaywallPolygonShape(
    private val sides: Int,
    private val cornerRadius: Dp,
    private val rotationDegrees: Float = -90f,
    private val innerRadiusScale: Float = 1.0f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val rx = size.width / 2f
        val ry = size.height / 2f
        val center = Offset(rx, ry)
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        val totalPoints = if (innerRadiusScale < 1.0f) sides * 2 else sides
        val angleStep = (2 * PI / totalPoints).toFloat()
        val startAngle = (rotationDegrees * PI / 180f).toFloat()

        val vertices = (0 until totalPoints).map { i ->
            val angle = startAngle + i * angleStep
            val scale = if (i % 2 == 1 && innerRadiusScale < 1.0f) innerRadiusScale else 1.0f
            Offset(
                x = center.x + rx * scale * cos(angle),
                y = center.y + ry * scale * sin(angle)
            )
        }

        val path = Path()
        for (i in vertices.indices) {
            val prev = vertices[(i - 1 + totalPoints) % totalPoints]
            val curr = vertices[i]
            val next = vertices[(i + 1) % totalPoints]

            val toPrev = prev - curr
            val toNext = next - curr
            val cutPrev = minOf(cornerRadiusPx, toPrev.getDistance() / 2f)
            val cutNext = minOf(cornerRadiusPx, toNext.getDistance() / 2f)

            val startPoint = curr + toPrev * (cutPrev / toPrev.getDistance())
            val endPoint = curr + toNext * (cutNext / toNext.getDistance())

            if (i == 0) path.moveTo(startPoint.x, startPoint.y) else path.lineTo(startPoint.x, startPoint.y)
            path.quadraticTo(curr.x, curr.y, endPoint.x, endPoint.y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/** Named shape presets */
object NewPaywallShapes {
    /** Shape 1: Horizontal Flat-topped Hexagon */
    val SaveBadge: Shape = NewPaywallPolygonShape(
        sides = 6,
        cornerRadius = 4.dp,
        rotationDegrees = 0f
    )

    /** Shape 2: Vertical Pointy Hexagon */
    val HexagonBadge: Shape = NewPaywallPolygonShape(
        sides = 6,
        cornerRadius = 6.dp,
        rotationDegrees = -90f
    )

    /** Shape 3: Spiked Starburst Seal */
    val PromoSeal: Shape = NewPaywallPolygonShape(
        sides = 12,
        cornerRadius = 2.dp,
        rotationDegrees = -90f,
        innerRadiusScale = 0.80f
    )
}

// ============================================================================
// INDIVIDUAL BADGE COMPOSABLES
// ============================================================================

/** Badge 1: Save 20% Pill Badge (Flat Hexagon) */
@Composable
fun Save20PillBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(NewPaywallShapes.SaveBadge)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                )
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SAVE 20%",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Badge 2: Pointy Hexagon Badge */
@Composable
fun PointyHexagonBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(NewPaywallShapes.HexagonBadge)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "SAVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = "20%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

/** Badge 3: Spiked Starburst Seal Badge */
@Composable
fun SpikedSave20SealBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(NewPaywallShapes.PromoSeal)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "SAVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = "20%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}