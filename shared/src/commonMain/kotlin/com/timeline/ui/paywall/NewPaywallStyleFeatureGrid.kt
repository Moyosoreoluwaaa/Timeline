package com.timeline.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.kmp.models.Package
import com.timeline.presentation.PaywallEvent
import com.timeline.presentation.PaywallState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun NewPaywallStyleFeatureGrid(
    state: PaywallState,
    onEvent: (PaywallEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPackage by remember(state.offerings) { 
        mutableStateOf(state.yearlyPackage ?: state.monthlyPackage) 
    }

    NewPaywallGradientBackdrop(
        topColor = NewPaywallPalette.GradientCyan,
        midColor = NewPaywallPalette.GradientTeal
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
                title = "See everything, all the time.",
                subtitle = "Everything you need to understand your habits."
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            val features = listOf(
                Icons.Default.CalendarMonth to AppStrings.PaywallUnlimitedHistory,
                Icons.Default.Mail to AppStrings.PaywallDailyMonthlyDigest,
                Icons.Default.BarChart to AppStrings.PaywallAdvancedInsights,
                Icons.Default.ElectricBolt to AppStrings.PaywallPrioritySupport
            )
            FeatureGrid(features)

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            BillingToggle(
                packages = state.currentOffering?.availablePackages ?: emptyList(),
                selectedPackage = selectedPackage,
                onSelect = { selectedPackage = it }
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            selectedPackage?.let { pkg ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = pkg.storeProduct.price.formatted,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NewPaywallPalette.OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Half))
                    Text(
                        text = if (pkg.identifier == state.yearlyPackage?.identifier) "/ year" else "/ month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NewPaywallPalette.OnBackgroundMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (pkg.identifier == state.yearlyPackage?.identifier) {
                        Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                        Save20PillBadge()
                    }
                }
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

@Composable
private fun FeatureGrid(features: List<Pair<ImageVector, String>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NewPaywallPalette.CardSurfaceElevated,
                            NewPaywallPalette.CardSurface
                        )
                    )
                )
                .padding(Dimensions.PaddingMedium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)) {
                features.chunked(2).forEach { rowFeatures ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)) {
                        rowFeatures.forEach { (icon, label) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = NewPaywallPalette.CardSurface,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(Dimensions.PaddingMedium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(NewPaywallShapes.PromoSeal)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        NewPaywallPalette.GradientCyan,
                                                        NewPaywallPalette.GradientTeal
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(Dimensions.IconSmall)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NewPaywallPalette.OnBackground
                                    )
                                }
                            }
                        }
                        if (rowFeatures.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BillingToggle(
    packages: List<Package>, 
    selectedPackage: Package?, 
    onSelect: (Package) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NewPaywallPalette.CardSurface, RoundedCornerShape(50)),
    ) {
        packages.forEach { pkg ->
            val selected = pkg.identifier == selectedPackage?.identifier
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimensions.Quat)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) NewPaywallPalette.OnBackground else Color.Transparent)
                    .clickable { onSelect(pkg) }
                    .padding(vertical = Dimensions.PaddingSmall),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pkg.storeProduct.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) NewPaywallPalette.Background else NewPaywallPalette.OnBackgroundMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
