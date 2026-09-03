package com.timeline.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.timeline.presentation.AuthState
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun AuthHeader() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.PaddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppStrings.AppName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingColossal)) // 96dp
            Surface(
                modifier = Modifier.size(Dimensions.IconHuge),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.SurfaceVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.displaySmall)
                }
            }
        }
    }
}

@Composable
fun AuthForm(
    state: AuthState,
    onSignInGoogle: () -> Unit,
    onSignInApple: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = Dimensions.PaddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = AppStrings.AuthWelcomeTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = AppStrings.AuthWelcomeSubtitle,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Subtitle)),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        AuthButton(text = AppStrings.AuthSignInGoogle, icon = { PlaceholderIcons.GoogleIcon() }, onClick = onSignInGoogle, enabled = !state.isLoading)
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
//        Text(text = AppStrings.AuthOr, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Scrim)))
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
//        AuthButton(text = AppStrings.AuthSignInApple, icon = { PlaceholderIcons.AppleIcon() }, onClick = onSignInApple, enabled = !state.isLoading)
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Spacer(modifier = Modifier.height(Dimensions.SpacingHuge))
    }
}

@Composable
fun AuthButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.ButtonHeight),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
