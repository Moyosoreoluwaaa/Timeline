package com.timeline.ui.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.PaywallEvent
import com.timeline.presentation.PaywallState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun NewPaywallStyleLimitedOffer(
    state: PaywallState,
    onEvent: (PaywallEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPackage by remember(state.offerings) { 
        mutableStateOf(state.yearlyPackage ?: state.monthlyPackage) 
    }
    val colors = NewPaywallPalette.colors

    NewPaywallGradientBackdrop(
        topColor = colors.gradientOrange,
        midColor = colors.gradientYellow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.Start
        ) {
            NewPaywallTopBar(onClose = onDismiss)
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            ProBadge()
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            NewPaywallHeadline(
                title = AppStrings.PaywallUnlockDeeper,
                subtitle = AppStrings.PaywallGoBeyond
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            NewPaywallFeatureRow(Icons.Default.CalendarMonth, AppStrings.PaywallUnlimitedHistory)
            NewPaywallFeatureRow(Icons.Default.Mail, AppStrings.PaywallDailyMonthlyDigest)
            NewPaywallFeatureRow(Icons.Default.BarChart, AppStrings.PaywallAdvancedInsights)
            NewPaywallFeatureRow(Icons.Default.ElectricBolt, AppStrings.PaywallPrioritySupport)

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, colors.goldCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colors.goldCardBackgroundTop, colors.goldCardBackgroundBottom)
                            )
                        )
                        .padding(Dimensions.PaddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AppStrings.PaywallLimitedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.goldAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = AppStrings.Paywall50Off,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.goldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.yearlyPackage?.storeProduct?.price?.formatted ?: AppStrings.PaywallThenPrice,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBackgroundMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            state.currentOffering?.availablePackages?.forEach { rcPackage ->
                NewPaywallPlanRow(
                    title = rcPackage.storeProduct.title,
                    subtitle = rcPackage.storeProduct.price.formatted,
                    selected = selectedPackage?.identifier == rcPackage.identifier,
                    onClick = { selectedPackage = rcPackage },
                    trailing = if (rcPackage.identifier == state.yearlyPackage?.identifier) {
                        { PointyHexagonBadge() }
                    } else null
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            }

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            NewPaywallPrimaryButton(AppStrings.PaywallStartTrial) { 
                selectedPackage?.let { onEvent(PaywallEvent.PurchasePackage(it)) }
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            NewPaywallFooterDisclaimer()
        }
    }
}
