// File: NewPaywallStyleClassic.kt
package com.timeline.ui.paywall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.timeline.ui.theme.Dimensions
import com.timeline.ui.theme.TimelineTheme
import com.timeline.util.AppStrings

@Composable
fun NewPaywallStyleClassic(
    onDismiss: () -> Unit,
    onStartTrial: (isYearly: Boolean) -> Unit
) {
    var isYearlySelected by remember { mutableStateOf(false) }
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
                title = AppStrings.PaywallUnlimitedInsights,
                subtitle = AppStrings.PaywallOneSimplePlan
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.cardSurface,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                    Text(
                        text = AppStrings.PaywallTimelinePro,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onBackground
                    )
                    Text(
                        text = AppStrings.PaywallMonthlyPrice,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBackgroundMuted
                    )
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                    NewPaywallFeatureRow(Icons.Default.CalendarMonth, AppStrings.PaywallUnlimitedHistory)
                    NewPaywallFeatureRow(Icons.Default.Mail, AppStrings.PaywallDailyMonthlyDigest)
                    NewPaywallFeatureRow(Icons.Default.BarChart, AppStrings.PaywallAdvancedInsights)
                    NewPaywallFeatureRow(Icons.Default.ElectricBolt, AppStrings.PaywallPrioritySupport)
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            NewPaywallPlanRow(
                title = AppStrings.PaywallMonthlyOption,
                subtitle = AppStrings.PaywallMonthlyBilling,
                selected = !isYearlySelected,
                onClick = { isYearlySelected = false }
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            NewPaywallPlanRow(
                title = AppStrings.PaywallYearlyOption,
                subtitle = AppStrings.PaywallYearlyBilling,
                selected = isYearlySelected,
                onClick = { isYearlySelected = true },
                trailing = { SpikedSave20SealBadge() }
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            NewPaywallPrimaryButton(AppStrings.PaywallStartTrial) { onStartTrial(isYearlySelected) }
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            NewPaywallFooterDisclaimer()
        }
    }
}

@Preview(showBackground = true, name = "Classic Dark")
@Composable
private fun NewPaywallStyleClassicDarkPreview() {
    TimelineTheme(darkTheme = true) {
        NewPaywallPalette.ProvideNewPaywallColors(darkTheme = true) {
            NewPaywallStyleClassic(onDismiss = {}, onStartTrial = {})
        }
    }
}

@Preview(showBackground = true, name = "Classic Light")
@Composable
private fun NewPaywallStyleClassicLightPreview() {
    TimelineTheme(darkTheme = false) {
        NewPaywallPalette.ProvideNewPaywallColors(darkTheme = false) {
            NewPaywallStyleClassic(onDismiss = {}, onStartTrial = {})
        }
    }
}