package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedSection by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.onEvent(SettingsEvent.LoadSettings) }

    Scaffold(
        topBar = { SettingsTopBar(onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TrackingSettingsSection(
                    state = state,
                    onUsageTrackingChange = { viewModel.onEvent(SettingsEvent.SetUsageTracking(it)) },
                    onScreenshotCaptureChange = { viewModel.onEvent(SettingsEvent.SetScreenshotCapture(it)) },
                    onFloatingOverlayChange = { viewModel.onEvent(SettingsEvent.SetFloatingOverlay(it)) }
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
                                Switch(checked = app.isExcluded, onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleExclusion(app.packageName)) })
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
            item { InfoCard(title = "About Timeline", description = "Learn more about the application.", icon = Icons.Default.Info) }
            item { InfoCard(title = "Application version", description = "1.0.0 (Stable)", icon = Icons.Default.Info) }

            item { SettingCategory("SUPPORT") }
            item { InfoCard(title = "Contact us", description = "Get help or provide feedback.", icon = Icons.AutoMirrored.Filled.ContactSupport) }
            item { InfoCard(title = "Report bugs", description = "Help us improve by reporting issues.", icon = Icons.Default.BugReport) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Everett is lightweight and event-driven.", style = MaterialTheme.typography.bodyMedium)
                            Text("No continuous tracking or background monitoring.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
