package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.kmp.models.Package
import com.timeline.domain.SubscriptionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {
    private val _state = MutableStateFlow(PaywallState())
    val state = _state.asStateFlow()

    private val _effects = Channel<PaywallEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            subscriptionManager.isPro.collect { isPro ->
                _state.update { it.copy(isPro = isPro) }
            }
        }
        loadOfferings()
    }

    fun onEvent(event: PaywallEvent) {
        when (event) {
            is PaywallEvent.LoadOfferings -> loadOfferings()
            is PaywallEvent.PurchasePackage -> purchase(event.rcPackage)
            is PaywallEvent.RestorePurchases -> restore()
        }
    }

    private fun loadOfferings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            subscriptionManager.initialize()
            _state.update { it.copy(
                offerings = subscriptionManager.offerings.value,
                isLoading = false
            ) }
        }
    }

    private fun purchase(rcPackage: Package) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = subscriptionManager.purchase(rcPackage)
            _state.update { it.copy(isLoading = false) }
            result.onSuccess {
                _effects.send(PaywallEffect.PurchaseSuccess)
            }.onFailure {
                _effects.send(PaywallEffect.PurchaseError(it.message ?: "Unknown error"))
            }
        }
    }

    private fun restore() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = subscriptionManager.restore()
            _state.update { it.copy(isLoading = false) }
            result.onSuccess {
                _effects.send(PaywallEffect.PurchaseSuccess)
            }.onFailure {
                _effects.send(PaywallEffect.PurchaseError(it.message ?: "Restore failed"))
            }
        }
    }
}
