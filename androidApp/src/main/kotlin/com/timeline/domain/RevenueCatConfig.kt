package com.timeline.domain

import android.content.Context
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import co.touchlab.kermit.Logger

val revenueCatApiKey: String = SecretConstants.REVENUECAT_ANDROID_KEY
val revenueCatEntitlementId: String = SecretConstants.REVENUECAT_ENTITLEMENT_ID

fun configureRevenueCat(context: Context) {
    Logger.withTag("RevenueCatConfig").d { "Configuring RevenueCat with API Key: $revenueCatApiKey" }
    Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(PurchasesConfiguration.Builder(context, revenueCatApiKey).build())
}
