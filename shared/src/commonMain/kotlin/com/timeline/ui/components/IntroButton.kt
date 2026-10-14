package com.timeline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timeline.ui.theme.AppShapes
import com.timeline.ui.theme.Dimensions

@Composable
internal fun ButtonLabel(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            fadeIn(onboardingTween(200)) togetherWith fadeOut(onboardingTween(150))
        },
        label = "ButtonLabelTransition"
    ) { targetText ->
        Text(
            text = targetText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
internal fun IntroButton(variant: FanVariant, label: String, onClick: () -> Unit) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.ButtonHeight)

    when (variant) {
        FanVariant.CenteredFan -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = introPalette.ink,
                contentColor = introPalette.background
            )
        ) { ButtonLabel(label) }

        FanVariant.CardHand -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = introPalette.accent,
                contentColor = introPalette.onAccent
            )
        ) { ButtonLabel(label) }

        FanVariant.ScreenshotWall -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = introPalette.card,
                contentColor = introPalette.textPrimary
            ),
            border = BorderStroke(1.dp, introPalette.stroke),
            contentPadding = PaddingValues(start = 22.dp, end = 6.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonLabel(label)
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(introPalette.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = introPalette.onAccent
                    )
                }
            }
        }
    }
}
