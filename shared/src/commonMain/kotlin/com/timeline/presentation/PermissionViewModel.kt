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

class PermissionViewModel : ViewModel() {
    private val _state = MutableStateFlow(PermissionState())
    val state = _state.asStateFlow()

    private val _effects = Channel<PermissionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: PermissionEvent) {
        when (event) {
            is PermissionEvent.GrantPermission -> grantPermission(event.id)
        }
    }

    private fun grantPermission(id: String) {
        viewModelScope.launch {
            _state.update { s ->
                s.copy(permissions = s.permissions.map {
                    if (it.id == id) it.copy(isVerifying = true) else it
                })
            }
            
            delay(1000) // Simulate intent/verification delay
            
            _state.update { s ->
                val newPermissions = s.permissions.map {
                    if (it.id == id) it.copy(isVerifying = false, isGranted = true) else it
                }
                s.copy(
                    permissions = newPermissions,
                    allGranted = newPermissions.all { it.isGranted }
                )
            }
            
            if (_state.value.allGranted) {
                _effects.send(PermissionEffect.AllGranted)
            }
        }
    }
}
