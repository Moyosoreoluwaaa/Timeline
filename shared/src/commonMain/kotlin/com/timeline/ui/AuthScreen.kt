package com.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.AuthEffect
import com.timeline.presentation.AuthEvent
import com.timeline.presentation.AuthViewModel
import com.timeline.ui.components.AuthForm
import com.timeline.ui.components.AuthHeader
import com.timeline.ui.theme.AppAlpha
import com.timeline.domain.auth.AuthUiHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    platformContext: Any? = null,
    onAuthSuccess: () -> Unit,
    onExitApp: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val authUiHelper: AuthUiHelper = koinInject()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            if (effect is AuthEffect.AuthSuccess) {
                onAuthSuccess()
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
            AuthHeader()
            AuthForm(
                state = state,
                onSignInGoogle = {
                    viewModel.onEvent(AuthEvent.SignInWithGoogle)
                    scope.launch {
                        platformContext?.let { ctx ->
                            authUiHelper.getGoogleIdToken(ctx)
                                .onSuccess { token ->
                                    viewModel.onEvent(AuthEvent.GoogleIdTokenReceived(token))
                                }
                                .onFailure { error ->
                                    viewModel.onEvent(AuthEvent.AuthError(error.message ?: "Cancelled"))
                                }
                        } ?: run {
                            viewModel.onEvent(AuthEvent.AuthError("No platform context"))
                        }
                    }
                },
                onSignInApple = { /* TODO */ }
            )

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Show global overlay when loading or authenticated (to prevent flashes during transitions)
        AnimatedVisibility(
            visible = state.isLoading || state.isAuthenticated,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = AppAlpha.Scrim)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
