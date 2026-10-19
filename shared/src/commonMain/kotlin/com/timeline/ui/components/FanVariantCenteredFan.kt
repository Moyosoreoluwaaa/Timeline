package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.timeline.presentation.PermissionEvent
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.on_balance

internal data class ShotSpec(val rotation: Float, val dx: Dp, val dy: Dp, val accentAlpha: Float?)
internal data class FanSpec(val widthFraction: Float, val shots: List<ShotSpec>)

internal val CenteredFanSpec = FanSpec(
    widthFraction = 0.70f,
    shots = listOf(
        ShotSpec(-11f, (-16).m, (-4).m, accentAlpha = 0.40f),
        ShotSpec(8f, 14.m, 0.m, accentAlpha = null),
    )
)

@Composable
internal fun CardFan(
    spec: FanSpec,
    page: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
) {
    val showFan = page == 0 || page == FOCUS_PAGE
    val showBadges = page == 0 || page == FOCUS_PAGE
    val mirrorBadges = page == FOCUS_PAGE

    val backCardImage = painterResource(Res.drawable.on_balance)
    val frontTiltState = animateFloatAsState(
        targetValue = if (page == 0) -7f else 0f,
        animationSpec = onboardingTween(250),
        label = "FrontTilt"
    )

    Box(
        Modifier
            .fillMaxWidth(spec.widthFraction)
            .wrapContentHeight()
    ) {
        AnimatedVisibility(
            visible = showFan,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            enter = fadeIn(onboardingTween(250)),
            exit = fadeOut(onboardingTween(150))
        ) {
            Box(Modifier.fillMaxSize()) {
                spec.shots.forEachIndexed { i, shot ->
                    val isPage1Back = page == 0 && i == 1
                    MockShot(
                        spec = if (isPage1Back) shot.copy(
                            rotation = shot.rotation + 10f,
                            dx = shot.dx + 8.m
                        ) else shot,
                        modifier = Modifier.fillMaxSize(),
                        image = if (isPage1Back) backCardImage else null
                    )
                }
            }
        }
        ConceptCardHost(
            page, focusAreas, onEvent,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { rotationZ = frontTiltState.value }
        )
        if (showBadges) FanBadges(mirrored = mirrorBadges)
    }
}

@Composable
internal fun BoxScope.FanBadges(mirrored: Boolean = false) {
    Box(
        Modifier
            .align(if (mirrored) Alignment.TopEnd else Alignment.TopStart)
            .offset(x = if (mirrored) 18.dp else (-18).dp, y = (-28).dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(introPalette.accent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Photo, null,
            tint = introPalette.onAccent, modifier = Modifier.size(20.dp)
        )
    }
    Box(
        Modifier
            .align(if (mirrored) Alignment.BottomStart else Alignment.BottomEnd)
            .offset(x = if (mirrored) (-16).dp else 16.dp, y = 32.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(introPalette.card)
            .border(0.5.dp, introPalette.stroke, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Schedule, null,
            tint = introPalette.accent, modifier = Modifier.size(18.dp)
        )
    }
}

internal fun Modifier.overhang(top: Dp, bottom: Dp): Modifier = layout { measurable, constraints ->
    val topPx = top.roundToPx()
    val bottomPx = bottom.roundToPx()
    val w = constraints.maxWidth
    val h = constraints.maxHeight
    val placeable = measurable.measure(Constraints.fixed(w, h + topPx + bottomPx))
    layout(w, h) { placeable.place(0, -topPx) }
}

@Composable
internal fun MockShot(spec: ShotSpec, modifier: Modifier = Modifier, image: Painter? = null) {
    val shape = RoundedCornerShape(14.dp)
    val headerColor =
        spec.accentAlpha?.let { introPalette.accent.copy(alpha = it) } ?: introPalette.ink.copy(alpha = 0.10f)
    Column(
        modifier
            .overhang(top = 16.m, bottom = 34.m)
            .graphicsLayer {
                rotationZ = spec.rotation
                translationX = spec.dx.toPx()
                translationY = spec.dy.toPx()
            }
            .clip(shape)
            .background(introPalette.shot)
            .border(0.5.dp, introPalette.stroke, shape)
    ) {
        Box(Modifier.fillMaxWidth().height(34.m).background(headerColor))
        if (image != null) {
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            Column(Modifier.padding(10.m)) { }
        }
    }
}
