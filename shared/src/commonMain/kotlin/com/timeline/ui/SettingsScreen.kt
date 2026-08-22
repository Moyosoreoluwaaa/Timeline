package com.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedSection by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsEvent.LoadSettings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SettingCategory("TRACKING") }
            item {
                SettingCard(
                    title = "Usage tracking",
                    description = "Record app usage activity.",
                    trailing = {
                        Switch(
                            checked = state.isUsageTrackingEnabled,
                            onCheckedChange = { viewModel.onEvent(SettingsEvent.SetUsageTracking(it)) }
                        )
                    }
                )
            }

            item { SettingCategory("CAPTURE") }
            item {
                SettingCard(
                    title = "Screenshot capture",
                    description = "Save snapshots during sessions.",
                    trailing = {
                        Switch(
                            checked = state.isScreenshotCaptureEnabled,
                            onCheckedChange = { viewModel.onEvent(SettingsEvent.SetScreenshotCapture(it)) }
                        )
                    }
                )
            }

            item { SettingCategory("OVERLAY") }
            item {
                SettingCard(
                    title = "Floating overlay",
                    description = "Show minimal indicator.",
                    trailing = {
                        Switch(
                            checked = state.isFloatingOverlayEnabled,
                            onCheckedChange = { viewModel.onEvent(SettingsEvent.SetFloatingOverlay(it)) }
                        )
                    }
                )
            }

            item { SettingCategory("PERMISSIONS") }
            item {
                SettingCard(
                    title = "App Exclusions",
                    description = "Manage apps not to be recorded.",
                    isExpandable = true,
                    isExpanded = expandedSection == "permissions",
                    onHeaderClick = { expandedSection = if (expandedSection == "permissions") null else "permissions" }
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        state.availableApps.forEach { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                                Switch(
                                    checked = app.isExcluded,
                                    onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleExclusion(app.packageName)) }
                                )
                            }
                        }
                    }
                }
            }

            item { SettingCategory("DATA") }
            item {
                SettingCard(
                    title = "Data retention",
                    description = "Keep data for ${state.dataRetentionDays} days",
                    isExpandable = true,
                    isExpanded = expandedSection == "data",
                    onHeaderClick = { expandedSection = if (expandedSection == "data") null else "data" }
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Select retention period")
                        Slider(
                            value = state.dataRetentionDays.toFloat(),
                            onValueChange = { viewModel.onEvent(SettingsEvent.SetDataRetention(it.toInt())) },
                            valueRange = 1f..365f,
                            steps = 364
                        )
                    }
                }
            }

            item { SettingCategory("ABOUT") }
            item {
                InfoCard(
                    title = "About Timeline",
                    description = "Learn more about the application.",
                    icon = Icons.Default.Info
                )
            }
            item {
                InfoCard(
                    title = "Application version",
                    description = "1.0.0 (Stable)",
                    icon = Icons.Default.Info
                )
            }

            item { SettingCategory("SUPPORT") }
            item {
                InfoCard(
                    title = "Contact us",
                    description = "Get help or provide feedback.",
                    icon = Icons.AutoMirrored.Filled.ContactSupport
                )
            }
            item {
                InfoCard(
                    title = "Report bugs",
                    description = "Help us improve by reporting issues.",
                    icon = Icons.Default.BugReport
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Everett is lightweight and event-driven.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "No continuous tracking or background monitoring.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (status != null) {
                            Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (isExpandable) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            if (isExpandable) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        content()
                    }
                }
            }
        }
    }
}
