package com.timeline.ui.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

/** Radial gradient blob anchored top-center, behind the header content of every style. */
@Composable
fun NewPaywallGradientBackdrop(
    topColor: Color,
    midColor: Color,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.4f,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = NewPaywallPalette.colors
    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(heightFraction)
        ) {
            val center = Offset(x = constraints.maxWidth / 2f, y = 0f)
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.radialGradient(
                        colors = listOf(topColor, midColor, Color.Transparent),
                        center = center,
                        radius = constraints.maxWidth.toFloat()
                    )
                )
            )
        }
        content()
    }
}

@Composable
fun NewPaywallTopBar(onClose: () -> Unit) {
    val colors = NewPaywallPalette.colors
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = AppStrings.AppName,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = AppStrings.ContentDescClose,
                tint = colors.onBackground
            )
        }
    }
}

@Composable
fun ProBadge(text: String = AppStrings.PaywallPro) {
    val colors = NewPaywallPalette.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.proBadgeBackground)
            .padding(horizontal = Dimensions.PaddingMedium, vertical = Dimensions.Half),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = null,
            tint = colors.proBadgeContent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(Dimensions.Half))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = colors.proBadgeContent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NewPaywallFeatureRow(
    icon: ImageVector,
    text: String,
    tint: Color = NewPaywallPalette.colors.onBackground
) {
    val colors = NewPaywallPalette.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimensions.Half)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimensions.IconSmall)
        )
        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground
        )
    }
}

/** A single billing option row — filled circle+check when selected, outline circle when not. */
@Composable
fun NewPaywallPlanRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = NewPaywallPalette.colors
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = colors.cardSurface,
        shape = MaterialTheme.shapes.medium,
        border = if (selected) BorderStroke(1.dp, colors.onBackgroundMuted) else null
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlanCheckCircle(selected = selected)
            Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onBackground
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onBackgroundMuted
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun PlanCheckCircle(selected: Boolean) {
    val colors = NewPaywallPalette.colors
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) colors.onBackground else Color.Transparent)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, colors.onBackgroundFaint, CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.background,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun NewPaywallPrimaryButton(text: String, onClick: () -> Unit) {
    val colors = NewPaywallPalette.colors
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.ctaContainer,
            contentColor = colors.ctaContent
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NewPaywallFooterDisclaimer(text: String = AppStrings.PaywallCancelAnytime) {
    val colors = NewPaywallPalette.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = colors.onBackgroundFaint,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NewPaywallHeadline(title: String, subtitle: String) {
    val colors = NewPaywallPalette.colors
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = colors.onBackground,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onBackgroundMuted,
        textAlign = TextAlign.Start
    )
}
