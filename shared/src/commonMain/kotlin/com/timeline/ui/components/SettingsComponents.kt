package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun UpgradeCard(
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onUpgradeClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppAlpha.SurfaceVariant)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
            Text(
                text = AppStrings.SettingsUpgradeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            
            PaywallFeatures()
            
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            val isDark = isSystemInDarkTheme()
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color.White else Color.Black,
                    contentColor = if (isDark) Color.Black else Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = AppStrings.PaywallStartTrial,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(AppStrings.SettingsTitle, style = MaterialTheme.typography.headlineLarge) },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = AppStrings.ContentDescBack)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}

@Composable
fun SettingCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier.padding(start = Dimensions.Half, bottom = Dimensions.Half)
    )
}

@Composable
fun InfoCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.CardOverlay)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(AppWeights.Full)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    status: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onHeaderClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isExpandable) Modifier.clickable(onClick = onHeaderClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.CardOverlay)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(AppWeights.Full)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (status != null) {
                            Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                        }
                        if (isExpandable) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            if (isExpandable) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Dimensions.PaddingSmall * 2), // 16dp
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppAlpha.Scrim)
                        )
                        content()
                    }
                }
            }
        }
    }
}
