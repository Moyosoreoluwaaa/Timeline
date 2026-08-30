package com.timeline.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

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
    val heightFraction = Dimensions.PermissionCardHeightBase + (stackIndex * Dimensions.PermissionCardHeightStep)

    var isCollapsing by remember { mutableStateOf(false) }

    LaunchedEffect(permission.isGranted) {
        if (permission.isGranted) {
            isCollapsing = true
        }
    }

    val collapseProgress by animateFloatAsState(
        targetValue = if (isCollapsing) 0f else 1f,
        animationSpec = tween(durationMillis = Dimensions.PermissionCollapseDurationMs, easing = FastOutSlowInEasing),
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
                    translationY = (1f - collapseProgress) * Dimensions.PermissionSlideOffset
                    alpha = collapseProgress
                    shadowElevation = (totalRemaining - stackIndex) * Dimensions.PermissionStackElevation.toPx()
                }
                .zIndex(zIndexValue),
            shape = RoundedCornerShape(topStart = Dimensions.PermissionCardCorner, topEnd = Dimensions.PermissionCardCorner),
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
                        color = AppColors.forPermission(permission.id).copy(alpha = AppAlpha.SurfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconForPermission(permission.id),
                                contentDescription = null,
                                tint = AppColors.forPermission(permission.id),
                                modifier = Modifier.size(Dimensions.IconMedium)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                    Text(text = permission.title, style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                    Text(
                        text = permission.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.Medium)),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

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


//---

private fun iconForPermission(id: String): ImageVector = when (id) {
    "usage" -> Icons.Rounded.BarChart
    "overlay" -> Icons.Rounded.Layers
    "notifications" -> Icons.Rounded.Notifications
    "accessibility" -> Icons.Rounded.AccessibilityNew
    "battery" -> Icons.Rounded.BatteryChargingFull
    else -> Icons.Rounded.Extension
}

/**
 * Thin segmented progress bar, one segment per permission, filled left-to-right
 * as permissions are granted. Lives at the very top of the screen — above the
 * headline and above the sheet — per the reference design.
 */
@Composable
fun PermissionTopIndicator(total: Int, filledCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Half)
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (index < filledCount) AppColors.BrandOrange
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppAlpha.Subtitle)
                    )
            )
        }
    }
}

/** Headline + subtitle. The old AppName brand line is intentionally dropped
 * here to match the reference design's single-headline layout — this is a
 * deliberate one-screen exception to the "unify the brand title everywhere"
 * recommendation from earlier, not an oversight. */
@Composable
fun PermissionHeaderText(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Subtitle)
        )
    }
}

/** Compact preview for a permission that hasn't become the active card yet —
 * literally the visible "top" of a not-yet-expanded card. */
@Composable
fun PermissionPeekChip(
    permission: PermissionItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.PermissionChipCorner))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.SurfaceVariant)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(Dimensions.PermissionChipCorner)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimensions.PaddingMedium, vertical = Dimensions.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(Dimensions.PermissionIconSize),
            shape = RoundedCornerShape(Dimensions.Default),
            color = AppColors.forPermission(permission.id).copy(alpha = AppAlpha.SurfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconForPermission(permission.id),
                    contentDescription = null,
                    tint = AppColors.forPermission(permission.id),
                    modifier = Modifier.size(Dimensions.IconSmall)
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
        Text(
            text = permission.title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Horizontally scrollable row of the queued (non-active) permissions. */
@Composable
fun PermissionPeekRow(
    permissions: List<PermissionItem>,
    selectedId: String?,
    onPermissionSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
        contentPadding = PaddingValues(horizontal = Dimensions.PaddingLarge)
    ) {
        items(permissions, key = { it.id }) { permission ->
            PermissionPeekChip(
                permission = permission,
                isSelected = permission.id == selectedId,
                onClick = { onPermissionSelected(permission.id) }
            )
        }
    }
}

/**
 * The expanded, currently-active permission — icon, title, description,
 * illustration, and the two action buttons. Fills whatever height its parent
 * gives it (the sheet hands it the remaining space below the peek row).
 */
@Composable
fun PermissionActiveCard(
    permission: PermissionItem,
    onGrant: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(Dimensions.PermissionActiveIconSize),
            shape = MaterialTheme.shapes.large,
            color = AppColors.forPermission(permission.id).copy(alpha = AppAlpha.SurfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconForPermission(permission.id),
                    contentDescription = null,
                    tint = AppColors.forPermission(permission.id),
                    modifier = Modifier.size(Dimensions.PermissionActiveIconInnerSize)
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(
            text = permission.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
        Text(
            text = permission.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.Medium),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        // TODO: Illustration block
        // if (permission.illustration != null) {
        //     AsyncImage(
        //         model = permission.illustration,
        //         contentDescription = permission.title,
        //         modifier = Modifier.size(Dimensions.PermissionIllustrationSize),
        //         contentScale = ContentScale.Fit
        //     )
        // } else {
        //     // Fallback while illustration assets aren't wired up yet.
        //     Icon(
        //         imageVector = iconForPermission(permission.id),
        //         contentDescription = null,
        //         tint = AppColors.forPermission(permission.id).copy(alpha = AppAlpha.Low),
        //         modifier = Modifier.size(Dimensions.IconHuge)
        //     )
        // }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        Button(
            onClick = onGrant,
            enabled = !permission.isVerifying && !permission.isGranted,
            modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (permission.isVerifying) {
                CircularProgressIndicator(modifier = Modifier.size(Dimensions.PaddingLarge), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(AppStrings.PermissionAllowButton, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

        Button(
            onClick = onNotNow,
            modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(AppStrings.PermissionNotNowButton, style = MaterialTheme.typography.titleMedium)
        }
    }
}
