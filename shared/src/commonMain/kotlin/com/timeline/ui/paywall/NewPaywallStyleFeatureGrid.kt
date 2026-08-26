// File: NewPaywallStyleFeatureGrid.kt
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timeline.ui.theme.Dimensions
import com.timeline.ui.theme.TimelineTheme
import com.timeline.util.AppStrings

@Composable
fun NewPaywallStyleFeatureGrid(
    onDismiss: () -> Unit,
    onStartTrial: (isYearly: Boolean) -> Unit
) {
    var isYearlySelected by remember { mutableStateOf(true) }

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
                isYearlySelected = isYearlySelected,
                onSelect = { isYearlySelected = it }
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (isYearlySelected) "$19.99" else "$3.99",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NewPaywallPalette.OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(Dimensions.Half))
                Text(
                    text = if (isYearlySelected) "/ year, after free trial" else "/ month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NewPaywallPalette.OnBackgroundMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (isYearlySelected) {
                    Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                    Save20PillBadge()
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            NewPaywallPrimaryButton(AppStrings.PaywallStartTrial) { onStartTrial(isYearlySelected) }
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
private fun BillingToggle(isYearlySelected: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NewPaywallPalette.CardSurface, RoundedCornerShape(50)),
    ) {
        listOf(false, true).forEach { yearly ->
            val selected = yearly == isYearlySelected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimensions.Quat)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) NewPaywallPalette.OnBackground else Color.Transparent)
                    .clickable { onSelect(yearly) }
                    .padding(vertical = Dimensions.PaddingSmall),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (yearly) AppStrings.PaywallYearlyOption else AppStrings.PaywallMonthlyOption,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) NewPaywallPalette.Background else NewPaywallPalette.OnBackgroundMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewPaywallStyleFeatureGridPreview() {
    TimelineTheme {
        NewPaywallStyleFeatureGrid(onDismiss = {}, onStartTrial = {})
    }
}