package com.timeline.domain

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionManager {
    val customerInfo: StateFlow<CustomerInfo?>
    val offerings: StateFlow<Offerings?>
    val isPro: StateFlow<Boolean>

    suspend fun initialize()
    suspend fun purchase(rcPackage: Package): Result<CustomerInfo>
    suspend fun restore(): Result<CustomerInfo>
    fun checkEntitlement(entitlementId: String): Boolean
}
