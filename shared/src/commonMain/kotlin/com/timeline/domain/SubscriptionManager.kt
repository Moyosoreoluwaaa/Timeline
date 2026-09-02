package com.timeline.domain

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionManager {
    val customerInfo: StateFlow<CustomerInfo?>
    val offerings: StateFlow<Offerings?>
    val isPro: StateFlow<Boolean>
    val isPurchasing: StateFlow<Boolean>
    val isRefreshing: StateFlow<Boolean>
    val error: StateFlow<String?>

    suspend fun initialize()
    suspend fun fetchOfferings()
    suspend fun purchase(rcPackage: Package): Result<CustomerInfo>
    suspend fun restore(): Result<CustomerInfo>
    fun checkEntitlement(entitlementId: String): Boolean

    suspend fun logIn(userId: String): Result<CustomerInfo>
    suspend fun logOut(): Result<CustomerInfo>
}
