package com.timeline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.PermissionEvent

@Composable
internal fun ConceptCardHost(
    page: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
        transitionSpec = {
            val forward = targetState > initialState
            val enter = fadeIn(onboardingTween(250)) + slideInVertically(animationSpec = onboardingTween(250)) { if (forward) it / 8 else -it / 8 }
            val exit = fadeOut(onboardingTween(150)) + slideOutVertically(animationSpec = onboardingTween(150)) { if (forward) -it / 8 else it / 8 }

            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "IntroCard"
    ) { p ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (p) {
                0 -> UsageCard()
                HIGHLIGHT_PAGE -> HighlightStack()
                EXCLUSION_PAGE -> ExclusionCard()
                NOTIFICATION_PAGE -> NotificationCard()
                else -> FocusCard(focusAreas) { onEvent(PermissionEvent.ToggleFocusArea(it)) }
            }
        }
    }
}

@Composable
internal fun IntroCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(introPalette.card)
            .border(0.5.dp, introPalette.stroke, shape)
            .padding(padding)
    ) { content() }
}

@Composable
internal fun IconTile(icon: ImageVector) {
    Box(
        Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(introPalette.accentSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = introPalette.accent,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
internal fun AppRow(
    icon: ImageVector,
    name: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = introPalette.textPrimary, fontSize = 14.sp)
            if (subtitle != null) {
                Text(subtitle, color = introPalette.textSecondary, fontSize = 11.sp)
            }
        }
        trailing()
    }
}

@Composable
internal fun Caption(text: String) {
    Text(text, color = introPalette.textSecondary, fontSize = 12.sp)
}
