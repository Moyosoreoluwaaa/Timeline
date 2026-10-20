package com.timeline.presentation

import com.timeline.domain.revenueCatEntitlementId

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.kmp.models.Package
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.timeline.analytics.Analytics
import com.timeline.analytics.AnalyticsEvents
import com.timeline.analytics.AnalyticsParams
import kotlin.time.Clock

class PaywallViewModel(
    private val subscriptionManager: SubscriptionManager,
    private val userPreferences: UserPreferences,
    private val analytics: Analytics
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
            analytics.log(AnalyticsEvents.PURCHASE_STARTED)
            _state.update { it.copy(isLoading = true) }
            val result = subscriptionManager.purchase(rcPackage)
            _state.update { it.copy(isLoading = false) }
            result.onSuccess { customerInfo ->
                val hasActiveEntitlements =
                    customerInfo.entitlements.active.containsKey(com.timeline.domain.revenueCatEntitlementId) ||
                    customerInfo.entitlements.active.isNotEmpty()
                if (hasActiveEntitlements) {
                    // Only mark trial start after a confirmed active entitlement
                    userPreferences.startTrial(Clock.System.now().toEpochMilliseconds())
                    _effects.send(PaywallEffect.PurchaseSuccess)
                } else {
                    _effects.send(PaywallEffect.PurchaseError("No active subscription detected after purchase."))
                }
            }.onFailure {
                _effects.send(PaywallEffect.PurchaseError(it.message ?: "Purchase failed"))
            }
        }
    }

    private fun restore() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = subscriptionManager.restore()
            _state.update { it.copy(isLoading = false) }
            result.onSuccess { customerInfo ->
                val hasActiveEntitlements = customerInfo.entitlements.active.isNotEmpty()
                val status = if (hasActiveEntitlements) "success" else "none_found"
                analytics.log(
                    AnalyticsEvents.RESTORE_TAPPED,
                    mapOf(AnalyticsParams.RESULT to status)
                )
                if (hasActiveEntitlements) {
                    _effects.send(PaywallEffect.RestoreSuccess)
                } else {
                    _effects.send(PaywallEffect.PurchaseError("No active subscription found to restore."))
                }
            }.onFailure {
                analytics.log(
                    AnalyticsEvents.RESTORE_TAPPED,
                    mapOf(AnalyticsParams.RESULT to "error")
                )
                _effects.send(PaywallEffect.PurchaseError(it.message ?: "Restore failed"))
            }
        }
    }
}
