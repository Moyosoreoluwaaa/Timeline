package com.timeline.domain

import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.timeline.util.ActivityTracker
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RevenueCatSubscriptionManager : SubscriptionManager, UpdatedCustomerInfoListener {
    
    private val logger = Logger.withTag("RevenueCatSubscriptionManager")

    companion object {
        var isFreeAccessEnabled: Boolean = false
    }

    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    override val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    override val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _isPro = MutableStateFlow(isFreeAccessEnabled)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    override val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override suspend fun initialize() {
        logger.d { "Initializing RevenueCat Subscription Manager" }
        logger.d { "Target Entitlement ID: $revenueCatEntitlementId" }
        
        Purchases.sharedInstance.updatedCustomerInfoListener = this
        refreshCustomerInfo()
        fetchOfferings()
    }

    private suspend fun refreshCustomerInfo() {
        try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            updateCustomerInfo(info)
        } catch (e: Exception) {
            logger.e(e) { "Failed to refresh customer info" }
        }
    }

    override suspend fun fetchOfferings() {
        try {
            _isRefreshing.value = true
            _error.value = null
            val fetchedOfferings = Purchases.sharedInstance.awaitOfferings()
            _offerings.value = fetchedOfferings
            logger.d { "Fetched offerings: ${fetchedOfferings.all.size}" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to fetch offerings" }
            _error.value = e.message
        } finally {
            _isRefreshing.value = false
        }
    }

    override suspend fun purchase(rcPackage: Package): Result<CustomerInfo> {
        if (_isPurchasing.value) return Result.failure(Exception("Purchase already in progress"))
        val activity = ActivityTracker.currentActivity
            ?: return Result.failure(IllegalStateException("No active Activity found for purchasing"))

        return try {
            _isPurchasing.value = true
            _error.value = null
            logger.d { "Starting purchase for package: ${rcPackage.identifier}" }
            
            val purchaseParams = PurchaseParams.Builder(activity, rcPackage).build()
            val purchaseResult = Purchases.sharedInstance.awaitPurchase(purchaseParams)
            updateCustomerInfo(purchaseResult.customerInfo)
            Result.success(purchaseResult.customerInfo)
        } catch (e: Exception) {
            if (e.message?.contains("user cancelled", ignoreCase = true) == true) {
                logger.d { "Purchase cancelled by user" }
            } else {
                logger.e(e) { "Purchase failed" }
                _error.value = e.message
            }
            Result.failure(e)
        } finally {
            _isPurchasing.value = false
        }
    }

    override suspend fun restore(): Result<CustomerInfo> {
        return try {
            _error.value = null
            logger.d { "Restoring purchases" }
            val info = Purchases.sharedInstance.awaitRestore()
            updateCustomerInfo(info)
            Result.success(info)
        } catch (e: Exception) {
            logger.e(e) { "Restore failed" }
            _error.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun logIn(userId: String): Result<CustomerInfo> {
        return try {
            logger.i { "Logging in to RevenueCat with UID: $userId" }
            val result = Purchases.sharedInstance.awaitLogIn(userId)
            updateCustomerInfo(result.customerInfo)
            Result.success(result.customerInfo)
        } catch (e: Exception) {
            logger.e(e) { "RevenueCat login failed" }
            Result.failure(e)
        }
    }

    override suspend fun logOut(): Result<CustomerInfo> {
        return try {
            logger.i { "Logging out from RevenueCat" }
            val info = Purchases.sharedInstance.awaitLogOut()
            updateCustomerInfo(info)
            Result.success(info)
        } catch (e: Exception) {
            logger.e(e) { "RevenueCat logout failed" }
            Result.failure(e)
        }
    }

    override fun checkEntitlement(entitlementId: String): Boolean =
        isFreeAccessEnabled || _customerInfo.value?.entitlements?.active?.containsKey(entitlementId) == true

    private fun updateCustomerInfo(info: CustomerInfo) {
        _customerInfo.value = info
        val proActive = isFreeAccessEnabled || info.entitlements.active.containsKey(revenueCatEntitlementId)
        _isPro.value = proActive
        logger.d { "Customer info updated. isFreeAccessEnabled: $isFreeAccessEnabled, isPro: $proActive" }
    }

    override fun onReceived(customerInfo: CustomerInfo) {
        updateCustomerInfo(customerInfo)
    }
}
