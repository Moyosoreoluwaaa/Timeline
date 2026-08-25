package com.timeline.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.*
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun PaywallFeatures() {
    Column {
        FeatureItem(Icons.Default.CalendarMonth, AppStrings.PaywallUnlimitedHistory)
        FeatureItem(Icons.Default.Mail, AppStrings.PaywallDailyMonthlyDigest)
        FeatureItem(Icons.Default.BarChart, AppStrings.PaywallAdvancedInsights)
        FeatureItem(Icons.Default.ElectricBolt, AppStrings.PaywallPrioritySupport)
    }
}

@Composable
fun PaywallBadge(text: String) {
    Surface(
        color = Color.White.copy(alpha = AppAlpha.SurfaceVariant),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.padding(bottom = Dimensions.PaddingSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Dimensions.Half, vertical = Dimensions.Quat)
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimensions.PaddingMedium)
            )
            Spacer(modifier = Modifier.width(Dimensions.Half))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimensions.Default)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.PaywallOfferGold,
            modifier = Modifier.size(Dimensions.IconSmall)
        )
        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
fun PlanOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) AppColors.PaywallOptionUnselected else Color.Transparent,
        shape = MaterialTheme.shapes.medium,
        border = if (selected) BorderStroke(Dimensions.Quat / 2, Color.White.copy(alpha = AppAlpha.Medium)) else null
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = AppAlpha.Medium)
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun PromoBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Brush.linearGradient(AppColors.BadgeGradient))
            .padding(horizontal = Dimensions.Default, vertical = Dimensions.Quat)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
