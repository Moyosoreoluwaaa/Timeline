package com.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.AuthEffect
import com.timeline.presentation.AuthEvent
import com.timeline.presentation.AuthViewModel
import com.timeline.ui.components.AuthForm
import com.timeline.ui.components.AuthHeader
import com.timeline.ui.theme.AppAlpha

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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
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
