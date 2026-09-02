package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeline.domain.usecase.SignInWithGoogleUseCase
import com.timeline.domain.usecase.SyncUserAccountUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val syncUserAccountUseCase: SyncUserAccountUseCase,
    private val logger: Logger
) : ViewModel() {
    private val tagLogger = logger.withTag("AuthViewModel")

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.GoogleIdTokenReceived -> handleGoogleIdToken(event.idToken)
            is AuthEvent.SignInWithGoogle -> {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            is AuthEvent.AuthError -> {
                _state.update { it.copy(isLoading = false, error = event.message) }
            }
            is AuthEvent.SignInWithApple,
            is AuthEvent.SignInWithEmail -> { /* Not implemented yet */ }
        }
    }

    private fun handleGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            tagLogger.i { "Processing Google ID token" }
            _state.update { it.copy(isLoading = true, error = null) }
            
            signInWithGoogleUseCase(idToken)
                .onSuccess { user ->
                    tagLogger.i { "Firebase Auth success, starting account sync" }
                    syncUserAccountUseCase(user.uid)
                        .onSuccess {
                            tagLogger.i { "Account sync complete" }
                            // Keep isLoading = true to prevent the button from flashing 
                            // before navigation transitions the screen.
                            _state.update { it.copy(isAuthenticated = true) }
                            _effects.send(AuthEffect.AuthSuccess)
                        }
                        .onFailure { e ->
                            tagLogger.e(e) { "Account sync failed" }
                            _state.update { it.copy(isLoading = false, error = e.message) }
                        }
                }
                .onFailure { e ->
                    tagLogger.e(e) { "Firebase Auth failed" }
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
