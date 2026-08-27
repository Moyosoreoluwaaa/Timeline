package com.timeline.domain

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RevenueCatSubscriptionManager : SubscriptionManager, PurchasesDelegate {
    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    override val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    override val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun initialize() {
        Purchases.sharedInstance.delegate = this
        refreshCustomerInfo()
        loadOfferings()
    }

    private suspend fun refreshCustomerInfo() {
        try {
            updateCustomerInfo(Purchases.sharedInstance.awaitCustomerInfo())
        } catch (e: Exception) { /* handle */ }
    }

    private suspend fun loadOfferings() {
        try {
            _offerings.value = Purchases.sharedInstance.awaitOfferings()
        } catch (e: Exception) { /* handle */ }
    }

    override suspend fun purchase(rcPackage: Package): Result<CustomerInfo> {
        return try {
            val info = Purchases.sharedInstance.awaitPurchase(rcPackage)
            updateCustomerInfo(info)
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restore(): Result<CustomerInfo> {
        return try {
            val info = Purchases.sharedInstance.awaitRestore()
            updateCustomerInfo(info)
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun checkEntitlement(entitlementId: String): Boolean =
        _customerInfo.value?.entitlements?.active?.containsKey(entitlementId) == true

    private fun updateCustomerInfo(info: CustomerInfo) {
        _customerInfo.value = info
        _isPro.value = info.entitlements.active.containsKey("timeline_pro")
    }

    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
        updateCustomerInfo(customerInfo)
    }

    override fun onPurchasePromoProduct(
        product: StoreProduct,
        startPurchase: (onError: (PurchasesError, Boolean) -> Unit, onSuccess: (StoreTransaction, CustomerInfo) -> Unit) -> Unit
    ) {
        startPurchase({ _, _ -> }, { _, info -> updateCustomerInfo(info) })
    }
}

// --- Coroutine bridges: purchases-kmp-core is callback-based; wrap each call once here ---

private suspend fun Purchases.awaitCustomerInfo(): CustomerInfo =
    suspendCancellableCoroutine { continuation ->
        getCustomerInfo(
            onSuccess = { continuation.resume(it) },
            onError = { error -> continuation.resumeWith(Result.failure(Exception(error.message))) }
        )
    }

private suspend fun Purchases.awaitOfferings(): Offerings =
    suspendCancellableCoroutine { continuation ->
        getOfferings(
            onError = { error -> continuation.resumeWith(Result.failure(Exception(error.message))) },
            onSuccess = { continuation.resume(it) }
        )
    }

private suspend fun Purchases.awaitPurchase(rcPackage: Package): CustomerInfo =
    suspendCancellableCoroutine { continuation ->
        purchase(
            packageToPurchase = rcPackage,
            onError = { error, _ -> continuation.resumeWith(Result.failure(Exception(error.message))) },
            onSuccess = { _, customerInfo -> continuation.resume(customerInfo) }
        )
    }

private suspend fun Purchases.awaitRestore(): CustomerInfo =
    suspendCancellableCoroutine { continuation ->
        restorePurchases(
            onError = { error -> continuation.resumeWith(Result.failure(Exception(error.message))) },
            onSuccess = { continuation.resume(it) }
        )
    }