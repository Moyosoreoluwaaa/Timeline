package com.timeline.ui.paywall

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.timeline.presentation.PaywallEffect
import com.timeline.presentation.PaywallViewModel
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.models.StoreTransaction

@Composable
fun RCPaywallScreen(
    viewModel: PaywallViewModel,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val listener = remember {
        object : PaywallListener {
            override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                onPurchaseSuccess()
            }
            override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                if (customerInfo.entitlements.active.containsKey("timeline_pro")) onPurchaseSuccess()
            }
        }
    }

    val options = remember {
        PaywallOptions(dismissRequest = onDismiss) {
            shouldDisplayDismissButton = true
            this.listener = listener
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading && state.offerings == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Paywall(options)
        }
    }
}