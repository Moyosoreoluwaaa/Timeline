package com.timeline.ui.paywall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timeline.presentation.PaywallEvent
import com.timeline.presentation.PaywallState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

private data class ComparisonRow(val label: String, val inFree: Boolean, val inPro: Boolean)

@Composable
fun NewPaywallStyleComparison(
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
                title = "Free vs PRO.",
                subtitle = "See exactly what you unlock."
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            ComparisonTable()
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            selectedPackage?.let { pkg ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val other = if (pkg.identifier == state.yearlyPackage?.identifier) state.monthlyPackage else state.yearlyPackage
                        other?.let { selectedPackage = it }
                    },
                    color = colors.cardSurface,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(Dimensions.PaddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pkg.storeProduct.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onBackground
                            )
                            Text(
                                text = pkg.storeProduct.price.formatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onBackgroundMuted
                            )
                        }
                        if (pkg.identifier == state.yearlyPackage?.identifier) {
                            SpikedSave20SealBadge()
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            Text(
                text = if (selectedPackage?.identifier == state.yearlyPackage?.identifier) "Switch to monthly billing" else "Switch to yearly billing (save 20%)",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onBackgroundMuted,
                modifier = Modifier.clickable { 
                    val other = if (selectedPackage?.identifier == state.yearlyPackage?.identifier) state.monthlyPackage else state.yearlyPackage
                    other?.let { selectedPackage = it }
                }
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            NewPaywallPrimaryButton(AppStrings.PaywallStartTrial) {
                selectedPackage?.let { onEvent(PaywallEvent.PurchasePackage(it)) }
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            NewPaywallFooterDisclaimer()
        }
    }
}

@Composable
fun ComparisonTable() {
    val rows = listOf(
        ComparisonRow(AppStrings.PaywallUnlimitedHistory, inFree = false, inPro = true),
        ComparisonRow(AppStrings.PaywallDailyMonthlyDigest, inFree = false, inPro = true),
        ComparisonRow(AppStrings.PaywallAdvancedInsights, inFree = false, inPro = true),
        ComparisonRow("7-day history", inFree = true, inPro = true),
        ComparisonRow(AppStrings.PaywallPrioritySupport, inFree = false, inPro = true)
    )
    val columnWidth = 56.dp
    val colors = NewPaywallPalette.colors

    Surface(
        color = colors.cardSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Free",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onBackgroundMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(columnWidth)
                )
                Text(
                    text = "PRO",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.goldAccent,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(columnWidth)
                )
            }
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.Half),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonMark(row.inFree, modifier = Modifier.width(columnWidth))
                    ComparisonMark(row.inPro, tint = colors.goldAccent, modifier = Modifier.width(columnWidth))
                }
            }
        }
    }
}

@Composable
private fun ComparisonMark(
    included: Boolean,
    tint: Color = NewPaywallPalette.colors.onBackground,
    modifier: Modifier = Modifier
) {
    val colors = NewPaywallPalette.colors
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (included) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = colors.crossMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
