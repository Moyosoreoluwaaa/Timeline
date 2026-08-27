package com.timeline.domain

import android.content.Context
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

class AndroidSubscriptionInitializer(private val context: Context) : SubscriptionInitializer {
    override fun configure(apiKey: String) {
        Purchases.configure(PurchasesConfiguration.Builder(apiKey).build())
    }
}
