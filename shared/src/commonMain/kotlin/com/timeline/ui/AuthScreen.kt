package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.AuthEffect
import com.timeline.presentation.AuthEvent
import com.timeline.presentation.AuthViewModel
import com.timeline.ui.components.AuthForm
import com.timeline.ui.components.AuthHeader
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AuthEffect.AuthSuccess -> onAuthSuccess()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(colors = AppColors.BrandGradient)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader(onClose = { /* Handle close */ })
            AuthForm(
                state = state,
                onSignInGoogle = { viewModel.onEvent(AuthEvent.SignInWithGoogle) },
                onSignInApple = { viewModel.onEvent(AuthEvent.SignInWithApple) }
            )
        }

        AnimatedVisibility(visible = state.isVerifying, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = AppAlpha.Scrim)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
