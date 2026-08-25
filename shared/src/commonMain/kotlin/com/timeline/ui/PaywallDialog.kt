package com.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.timeline.ui.components.FeatureItem
import com.timeline.ui.components.PaywallBadge
import com.timeline.ui.components.PlanOption
import com.timeline.ui.components.PromoBadge
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onStartTrial: (isYearly: Boolean) -> Unit,
    isDealsVariant: Boolean = false
) {
    var isYearlySelected by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // Base color for 100% opacity
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.BrandOrange,
                            AppColors.BrandYellow,
                            AppColors.BrandBlack
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Dimensions.PaddingLarge),
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = AppStrings.AppName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.ContentDescClose,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                PaywallBadge(AppStrings.PaywallPro)

                Text(
                    text = if (isDealsVariant) AppStrings.PaywallUnlockDeeper else AppStrings.PaywallUnlimitedInsights,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = if (isDealsVariant) AppStrings.PaywallGoBeyond else AppStrings.PaywallOneSimplePlan,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = AppAlpha.Subtitle),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

                if (!isDealsVariant) {
                    // Variant 1: Features inside a dark card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.PaywallCardBackground),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                            Text(
                                text = AppStrings.PaywallTimelinePro,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = AppStrings.PaywallMonthlyPrice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                            
                            PaywallFeatures()
                        }
                    }
                } else {
                    // Variant 2: Features directly on background
                    PaywallFeatures()
                    
                    Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

                    // Variant 2: Golden Limited Time Offer card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.PaywallCardBackground.copy(alpha = 0.5f)),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(Dimensions.LineThickness, AppColors.PaywallOfferGold)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(Dimensions.PaddingMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppStrings.PaywallLimitedTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.PaywallOfferGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = AppStrings.Paywall50Off,
                                style = MaterialTheme.typography.headlineMedium,
                                color = AppColors.PaywallOfferGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.PaywallThenPrice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = AppAlpha.Medium)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

                // Shared: Plan Selection
                PlanOption(
                    title = AppStrings.PaywallMonthlyOption,
                    subtitle = AppStrings.PaywallMonthlyBilling,
                    selected = !isYearlySelected,
                    onClick = { isYearlySelected = false }
                )
                
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))

                PlanOption(
                    title = AppStrings.PaywallYearlyOption,
                    subtitle = AppStrings.PaywallYearlyBilling,
                    selected = isYearlySelected,
                    onClick = { isYearlySelected = true },
                    trailing = { PromoBadge(AppStrings.PaywallSave50) }
                )

                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

                Button(
                    onClick = { onStartTrial(isYearlySelected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.ButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = AppStrings.PaywallStartTrial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))

                Text(
                    text = AppStrings.PaywallCancelAnytime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = AppAlpha.Low),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PaywallFeatures() {
    FeatureItem(Icons.Default.CalendarMonth, AppStrings.PaywallUnlimitedHistory)
    FeatureItem(Icons.Default.Mail, AppStrings.PaywallDailyMonthlyDigest)
    FeatureItem(Icons.Default.BarChart, AppStrings.PaywallAdvancedInsights)
    FeatureItem(Icons.Default.ElectricBolt, AppStrings.PaywallPrioritySupport)
}
