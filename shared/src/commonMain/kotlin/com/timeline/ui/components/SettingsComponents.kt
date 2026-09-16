package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
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
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onLogout: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    CustomTopAppBar(
        title = { Text(AppStrings.SettingsTitle, style = MaterialTheme.typography.headlineLarge) },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = AppStrings.ContentDescBack)
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isLoggedIn) {
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onLogout()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Sign In") },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Outlined.Login, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToAuth()
                            }
                        )
                    }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Subtitle))
            .clickable(onClick = onClick)
            .padding(Dimensions.PaddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
            Column(modifier = Modifier.weight(AppWeights.Full)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
//                Text(
//                    text = description,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
            }
            Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))

        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    status: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onHeaderClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val finalOnClick = onClick ?: if (isExpandable) onHeaderClick else null
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Subtitle))
            .then(if (finalOnClick != null) Modifier.clickable(onClick = finalOnClick) else Modifier)
            .padding(Dimensions.PaddingMedium)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(AppWeights.Full)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
//                    Text(
//                        text = description,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isExpandable) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (status != null) {
                            Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
                        }
                    }
                }
            }
            if (isExpandable) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Dimensions.PaddingSmall * 2),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppAlpha.Scrim)
                        )
                        content()
                    }
                }
            }
        }
    }
}