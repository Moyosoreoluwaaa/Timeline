package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.OnboardingStep
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.Dimensions

@Composable
fun OnboardingLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium)
            ) {
                topBar()
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(Dimensions.PaddingLarge)
            ) {
                bottomBar()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.Start,
            content = content
        )
    }
}

@Composable
fun OnboardingStepIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Default)
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(Dimensions.Default)
                    .clip(CircleShape)
                    .background(
                        if (index == current) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
            )
        }
    }
}

@Composable
fun OnboardingPermissionIndicator(
    current: Int,
    total: Int = 3
) {
    Text(
        text = "$current of $total",
        style = MaterialTheme.typography.labelLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun OnboardingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showArrow: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.onSurface,
    contentColor: Color = MaterialTheme.colorScheme.surface
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.ButtonHeight),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (showArrow) {
                Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun OnboardingIllustration(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun OnboardingIntroFeature(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OnboardingPermissionFeature(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.PaddingMedium, horizontal = Dimensions.PaddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
    }
}
