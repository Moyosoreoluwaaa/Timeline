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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.timeline.presentation.AuthState
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun AuthHeader(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Dimensions.PaddingMedium)
                .clip(CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = AppStrings.ContentDescClose, tint = MaterialTheme.colorScheme.onBackground)
        }
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.PaddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppStrings.AppName,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                ),
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
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium),
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
        AuthButton(text = AppStrings.AuthSignInApple, icon = { PlaceholderIcons.AppleIcon() }, onClick = onSignInApple, enabled = !state.isLoading)
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.AuthOr, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Scrim)))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text(AppStrings.AuthEmailPlaceholder, color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Scrim)) },
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Divider),
                focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.SurfaceVariant),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingHuge))
        Text(text = AppStrings.AuthNoAccount, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground))
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
            Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
