package com.timeline.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.ScreenshotImage
import kotlin.math.roundToInt
import androidx.compose.ui.unit.lerp as lerpDp

/** Clickable that reports the element's bounds in root coordinates. */
@Composable
fun Modifier.clickableWithBounds(enabled: Boolean = true, onClick: (Rect) -> Unit): Modifier {
    // Plain holder, not state: avoids recomposing while the sheet is being dragged
    val holder = remember { object { var coords: LayoutCoordinates? = null } }
    return this
        .onGloballyPositioned { holder.coords = it }
        .clickable(enabled = enabled) {
            holder.coords?.takeIf { it.isAttached }?.let { onClick(it.boundsInRoot()) }
        }
}

@Composable
fun ImageHeroOverlay(
    path: String?,
    originBounds: Rect?,
    onDismiss: () -> Unit,
    onBoundsCalculated: (TutorialStep, Rect) -> Unit = { _, _ -> },
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    var shownPath by remember { mutableStateOf<String?>(null) }
    var shownOrigin by remember { mutableStateOf<Rect?>(null) }
    var rootOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(path) {
        if (path != null) {
            shownPath = path
            shownOrigin = originBounds
            progress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        } else if (shownPath != null) {
            progress.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
            shownPath = null
        }
    }
    val current = shownPath ?: return

    Box(
        modifier
            .fillMaxSize()
            .zIndex(100f)
            .onGloballyPositioned { rootOffset = it.positionInRoot() }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = path != null,
                    onClick = onDismiss
                )
        )
        ScreenshotImage(
            path = current,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .layout { measurable, constraints ->
                    val w = constraints.maxWidth.toFloat()
                    val h = constraints.maxHeight.toFloat()
                    val target = Rect(w * 0.025f, h * 0.025f, w * 0.975f, h * 0.975f)
                    val start = shownOrigin?.translate(-rootOffset)
                        ?: Rect(w * 0.3f, h * 0.3f, w * 0.7f, h * 0.7f) // fallback: scale from center
                    val r = lerp(start, target, progress.value)
                    val placeable = measurable.measure(
                        Constraints.fixed(
                            r.width.roundToInt().coerceAtLeast(0),
                            r.height.roundToInt().coerceAtLeast(0)
                        )
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(r.left.roundToInt(), r.top.roundToInt())
                    }
                }
                .graphicsLayer {
                    shape = RoundedCornerShape(lerpDp(8.dp, 20.dp, progress.value).toPx())
                    clip = true
                }
                .spotlightTarget(TutorialStep.FULL_SCREEN_IMAGE_PREVIEW, onBoundsCalculated)
        )
    }
}