package com.timeline.domain

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import co.touchlab.kermit.Logger

expect val revenueCatApiKey: String
expect val revenueCatEntitlementId: String

fun configureRevenueCat() {
    Logger.withTag("RevenueCatConfig").d { "Configuring RevenueCat with API Key: $revenueCatApiKey" }
    Purchases.configure(apiKey = revenueCatApiKey)
}
