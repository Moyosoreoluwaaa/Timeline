package com.timeline.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun PermissionHeader(
    state: PermissionState
) {
    Column(
        modifier = Modifier.padding(horizontal = Dimensions.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(
            text = AppStrings.AppName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingHuge))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.PaddingSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.permissions.forEach { permission ->
                if (permission.isGranted) {
                    Box(
                        modifier = Modifier
                            .size(Dimensions.Default)
                            .clip(CircleShape)
                            .border(width = Dimensions.LineThickness, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(Dimensions.Default)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppAlpha.Subtitle))
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCard(
    permission: PermissionItem,
    stackIndex: Int,
    totalRemaining: Int,
    onGrant: () -> Unit,
    onAnimationFinished: () -> Unit = {}
) {
    val zIndexValue = (totalRemaining - stackIndex).toFloat()
    
    // Front card (stackIndex 0) is the shortest
    // Back cards are taller
    val heightFraction = 0.65f + (stackIndex * 0.08f)

    var isCollapsing by remember { mutableStateOf(false) }

    LaunchedEffect(permission.isGranted) {
        if (permission.isGranted) {
            isCollapsing = true
        }
    }

    val collapseProgress by animateFloatAsState(
        targetValue = if (isCollapsing) 0f else 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "Collapse",
        finishedListener = { if (it == 0f) onAnimationFinished() }
    )

    if (collapseProgress > 0f) {
        val orangeGradient = Brush.linearGradient(
            0.0f to AppColors.BrandOrange,
            0.2f to AppColors.BrandOrange,
            0.201f to Color.Transparent,
            1f to Color.Transparent,
            start = Offset(0f, 0f),
            end = Offset(400f, 400f)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .graphicsLayer {
                    translationY = (1f - collapseProgress) * 2000f
                    alpha = collapseProgress
                    shadowElevation = (totalRemaining - stackIndex) * 8f
                }
                .zIndex(zIndexValue),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, orangeGradient)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(Dimensions.PaddingLarge)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(Dimensions.IconLarge),
                        shape = MaterialTheme.shapes.medium,
                        color = when(permission.id) {
                            "usage" -> AppColors.PermissionUsage.copy(alpha = AppAlpha.SurfaceVariant)
                            "overlay" -> AppColors.PermissionOverlay.copy(alpha = AppAlpha.SurfaceVariant)
                            "notifications" -> AppColors.PermissionNotifications.copy(alpha = AppAlpha.SurfaceVariant)
                            "accessibility" -> AppColors.PermissionAccessibility.copy(alpha = AppAlpha.SurfaceVariant)
                            "battery" -> AppColors.PermissionBattery.copy(alpha = AppAlpha.SurfaceVariant)
                            else -> AppColors.PermissionDefault.copy(alpha = AppAlpha.SurfaceVariant)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                when(permission.id) {
                                    "usage" -> "\uD83D\uDCC8"
                                    "overlay" -> "\uD83D\uDDA5\ufe0f"
                                    "notifications" -> "\uD83D\uDD14"
                                    "accessibility" -> "\uD83D\uDE4B"
                                    "battery" -> "\uD83D\uDD0B"
                                    else -> "✨"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                    Text(text = permission.title, style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                    Text(text = permission.description, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.Medium)), textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = onGrant,
                        enabled = stackIndex == 0 && !permission.isVerifying && !permission.isGranted,
                        modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        if (permission.isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(Dimensions.PaddingLarge), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(AppStrings.PermissionAllowButton, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                    
                    Button(
                        onClick = { /* Not now */ },
                        modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = AppStrings.PermissionNotNowButton, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                }
            }
        }
    }
}
