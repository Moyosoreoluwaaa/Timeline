package com.timeline.presentation

import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package

data class PaywallState(
    val offerings: Offerings? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPro: Boolean = false
) {
    val currentOffering get() = offerings?.current
    val monthlyPackage get() = currentOffering?.monthly
    val yearlyPackage get() = currentOffering?.annual
}

sealed interface PaywallEvent {
    data object LoadOfferings : PaywallEvent
    data class PurchasePackage(val rcPackage: Package) : PaywallEvent
    data object RestorePurchases : PaywallEvent
}

sealed interface PaywallEffect {
    data object PurchaseSuccess : PaywallEffect
    data object RestoreSuccess : PaywallEffect
    data class PurchaseError(val error: String) : PaywallEffect
}
