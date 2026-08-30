package com.timeline.domain

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

class AndroidSubscriptionInitializer : SubscriptionInitializer {
    override fun configure(apiKey: String) {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(apiKey).build())
    }
}
