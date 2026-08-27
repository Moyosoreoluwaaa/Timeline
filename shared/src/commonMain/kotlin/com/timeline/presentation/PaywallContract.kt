package com.timeline.presentation

import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package

data class PaywallState(
    val offerings: Offerings? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPro: Boolean = false
) {
    val currentOffering get() = offerings?.current
    val monthlyPackage get() = currentOffering?.monthly
    val yearlyPackage get() = currentOffering?.annual
    val lifetimePackage get() = currentOffering?.lifetime
}

sealed interface PaywallEvent {
    data object LoadOfferings : PaywallEvent
    data class PurchasePackage(val rcPackage: Package) : PaywallEvent
    data object RestorePurchases : PaywallEvent
}

sealed interface PaywallEffect {
    data object PurchaseSuccess : PaywallEffect
    data class PurchaseError(val error: String) : PaywallEffect
}
