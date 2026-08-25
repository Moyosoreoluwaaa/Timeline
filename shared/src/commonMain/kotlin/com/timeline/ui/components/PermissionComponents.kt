package com.timeline.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PermissionHeader(
    onClose: () -> Unit,
    state: PermissionState
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Dimensions.PaddingMedium)
                .clip(CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = AppStrings.ContentDescClose, tint = MaterialTheme.colorScheme.onBackground)
        }

        Column(
            modifier = Modifier.padding(horizontal = Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
            Text(
                text = AppStrings.AppName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingHuge)) // 48dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .padding(vertical = Dimensions.PaddingSmall),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val grantedCount = state.permissions.count { it.isGranted }
                val totalCount = state.permissions.size
                repeat(totalCount) { index ->
                    Surface(
                        modifier = Modifier.size(Dimensions.PaddingSmall),
                        shape = CircleShape,
                        color = if (index < grantedCount) Color.Green else Color.Gray
                    ) {}
                    if (index < totalCount - 1) {
                        Box(modifier = Modifier.width(Dimensions.PaddingLarge).height(Dimensions.Quat / 2).background(Color.Gray))
                    }
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
    onGrant: () -> Unit
) {
    val isFront = stackIndex == 0
    val targetScale = 1f - (stackIndex * 0.05f)
    val targetOffset = -(Dimensions.PaddingLarge * stackIndex)
    val scale by animateFloatAsState(targetValue = targetScale, label = "Scale")
    val offset by animateDpAsState(targetValue = targetOffset, label = "Offset")
    val zIndexValue = (totalRemaining - stackIndex).toFloat()

    var showCheck by remember { mutableStateOf(false) }
    var isCollapsing by remember { mutableStateOf(false) }

    LaunchedEffect(permission.isGranted) {
        if (permission.isGranted) {
            showCheck = true
            delay(500.milliseconds)
            isCollapsing = true
        }
    }

    val collapseProgress by animateFloatAsState(
        targetValue = if (isCollapsing) 0f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    if (collapseProgress > 0f) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimensions.PaddingLarge)
                .offset(y = offset)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = (AppAlpha.Full - collapseProgress) * Dimensions.SlideDownOffset
                    alpha = collapseProgress
                }
                .zIndex(zIndexValue),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.padding(Dimensions.PaddingLarge)) {
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
                    Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
                    Button(
                        onClick = onGrant,
                        enabled = isFront && !permission.isVerifying && !permission.isGranted,
                        modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        if (permission.isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(Dimensions.PaddingLarge), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(AppStrings.PermissionAllowButton, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                    Text(text = AppStrings.PermissionNotNowButton, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.padding(vertical = Dimensions.PaddingSmall))
                }
                if (showCheck) {
                    Box(modifier = Modifier.matchParentSize().background(MaterialTheme.colorScheme.surface.copy(alpha = AppAlpha.CardOverlay)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(Dimensions.IconHuge))
                    }
                }
            }
        }
    }
}
