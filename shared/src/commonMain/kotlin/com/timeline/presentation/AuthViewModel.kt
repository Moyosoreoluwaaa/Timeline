package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SignInWithGoogle,
            is AuthEvent.SignInWithApple,
            is AuthEvent.SignInWithEmail -> startFakeAuth()
        }
    }

    private fun startFakeAuth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isVerifying = true) }
            delay(1000) // Simulate verification
            _state.update { it.copy(isLoading = false, isVerifying = false, isAuthenticated = true) }
            _effects.send(AuthEffect.AuthSuccess)
        }
    }
}
