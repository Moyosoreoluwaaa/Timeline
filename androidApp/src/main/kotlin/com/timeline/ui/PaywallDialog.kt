package com.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.timeline.ui.components.PaywallFeatures
import com.timeline.ui.components.PlanOption
import com.timeline.ui.components.PromoBadge
import com.timeline.ui.paywall.PaywallFooter
import com.timeline.ui.paywall.PaywallHeader
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onStartTrial: (isYearly: Boolean) -> Unit,
    isDealsVariant: Boolean = false,
    onRestorePurchases: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTermsOfUse: () -> Unit = {}
) {
    var isYearlySelected by remember { mutableStateOf(true) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Free-form radial gradient at the top with center offset
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
            ) {
                val center = Offset(x = constraints.maxWidth / 2f, y = 0f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColors.BrandYellow,
                                    AppColors.BrandYellow,
                                    AppColors.BrandOrange,
                                    Color.Transparent
                                ),
                                center = center,
                                radius = constraints.maxWidth.toFloat()
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Dimensions.PaddingLarge),
                horizontalAlignment = Alignment.Start
            ) {
                PaywallHeader(
                    isDealsVariant = isDealsVariant,
                    onDismiss = onDismiss
                )

                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

                if (!isDealsVariant) {
                    // Variant 1: Features inside a card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.PaywallCardBackground),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                            Text(
                                text = AppStrings.PaywallTimelinePro,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black
                            )
                            Text(
                                text = AppStrings.PaywallMonthlyPrice,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = AppAlpha.Medium)
                            )
                            
                            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                            
                            PaywallFeatures(contentColor = Color.Black)
                        }
                    }
                } else {
                    // Variant 2: Features directly on background
                    PaywallFeatures()
                    
                    Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

                    // Variant 2: Golden Limited Time Offer card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(Dimensions.LineThickness, AppColors.PaywallOfferGold)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(Dimensions.PaddingMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppStrings.PaywallLimitedTime,
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = AppColors.PaywallOfferGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.Paywall50Off,
                                style = MaterialTheme.typography.headlineMedium,
                                color = AppColors.PaywallOfferGold
                            )
                            Text(
                                text = AppStrings.PaywallThenPrice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Medium)
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = AppStrings.PaywallStartTrial,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))

                Text(
                    text = AppStrings.PaywallCancelAnytime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Low),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                PaywallFooter(
                    onRestorePurchases = onRestorePurchases,
                    onPrivacyPolicy = onPrivacyPolicy,
                    onTermsOfUse = onTermsOfUse,
                    uriHandler = uriHandler
                )
            }
        }
    }
}
