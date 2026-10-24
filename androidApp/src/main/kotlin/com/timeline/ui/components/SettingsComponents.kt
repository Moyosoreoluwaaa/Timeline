package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    isLoggedIn: Boolean,
    isPro: Boolean = false,
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onLogout: () -> Unit,
    onOpenAbout: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val privacyUrl = "https://v0-timeline-app.vercel.app/privacy"
    val termsUrl = "https://v0-timeline-app.vercel.app/terms"
    val yellowAccent = Color(0xFFFFD54F)

    CustomTopAppBar(
        title = {
            Column {
                Text(
                    AppStrings.SettingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isPro) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Free Plan",
                        style = MaterialTheme.typography.labelMedium.copy(color = yellowAccent, fontWeight = FontWeight.Medium)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = AppStrings.ContentDescBack
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("About") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOpenAbout()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Terms of Service") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            uriHandler.openUri(termsUrl)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Privacy Policy") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.PrivacyTip,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            uriHandler.openUri(privacyUrl)
                        }
                    )

//                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
//                    if (isLoggedIn) {
//                        DropdownMenuItem(
//                            text = { Text(AppStrings.SettingsSignOut) },
//                            leadingIcon = {
//                                Icon(
//                                    Icons.AutoMirrored.Outlined.Logout,
//                                    contentDescription = null
//                                )
//                            },
//                            onClick = { showMenu = false; onLogout() }
//                        )
//                    } else {
//                        DropdownMenuItem(
//                            text = { Text(AppStrings.SettingsSignIn) },
//                            leadingIcon = {
//                                Icon(
//                                    Icons.AutoMirrored.Outlined.Login,
//                                    contentDescription = null
//                                )
//                            },
//                            onClick = { showMenu = false; onNavigateToAuth() }
//                        )
//                    }
                    
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun UpgradeCard(
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Subtitle))
            .clickable { onUpgradeClick() }
            .padding(Dimensions.PaddingMedium)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = AppStrings.SettingsUpgradeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))

            PaywallFeatures()

            Spacer(modifier = Modifier.weight(1f))

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
                shape = com.timeline.ui.theme.AppShapes.Pill,
            ) {
                Text(
                    text = AppStrings.PaywallStartTrial,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SettingCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.secondary),
        modifier = Modifier.padding(start = Dimensions.Half, bottom = Dimensions.Half)
    )
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Subtitle),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(AppWeights.Full),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailing != null) trailing()
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Subtitle),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(horizontal = Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ValueSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    label: (Float) -> String
) {
    Column {
        Text(
            text = label(value),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
