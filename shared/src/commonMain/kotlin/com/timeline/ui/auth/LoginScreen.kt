package com.timeline.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.AuthViewModel
import com.timeline.presentation.AuthEvent
import com.timeline.presentation.AuthEffect
import com.timeline.domain.auth.AuthUiHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
    platformContext: Any? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val authUiHelper: AuthUiHelper = koinInject()
    
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            if (effect is AuthEffect.AuthSuccess) {
                onLoginSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Timeline",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (state.isLoading || state.isAuthenticated) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in with Google")
            }
        }
        
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
