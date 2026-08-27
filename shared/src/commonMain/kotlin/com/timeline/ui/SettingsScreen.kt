package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.components.InfoCard
import com.timeline.ui.components.SettingCard
import com.timeline.ui.components.SettingCategory
import com.timeline.ui.components.SettingsTopBar
import com.timeline.ui.components.UpgradeCard
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToPaywall: (isDeals: Boolean) -> Unit = {},
    onNavigateToCustomerCenter: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedSection by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.onEvent(SettingsEvent.LoadSettings) }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = { SettingsTopBar(onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = Dimensions.SpacingLarge, topEnd = Dimensions.SpacingLarge)),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimensions.PaddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
                    contentPadding = PaddingValues(
                        top = Dimensions.PaddingMedium,
                        bottom = navBarPadding + Dimensions.PaddingExtraLarge
                    )
                ) {
                    item {
                        UpgradeCard(onUpgradeClick = { onNavigateToPaywall(false) })
                    }

                    item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

                    item { SettingCategory(AppStrings.SettingsCategoryTracking) }
                    item {
                        SettingCard(
                            title = AppStrings.SettingsUsageTrackingTitle,
                            description = AppStrings.SettingsUsageTrackingDesc,
                            trailing = { Switch(checked = state.isUsageTrackingEnabled, onCheckedChange = { viewModel.onEvent(SettingsEvent.SetUsageTracking(it)) }) }
                        )
                    }
                    item {
                        SettingCard(
                            title = AppStrings.SettingsScreenshotCaptureTitle,
                            description = AppStrings.SettingsScreenshotCaptureDesc,
                            trailing = { Switch(checked = state.isScreenshotCaptureEnabled, onCheckedChange = { viewModel.onEvent(SettingsEvent.SetScreenshotCapture(it)) }) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

                    item { SettingCategory(AppStrings.SettingsCategoryData) }
                    item {
                        SettingCard(
                            title = "Manage Subscription",
                            description = "View and manage your active plans.",
                            onClick = onNavigateToCustomerCenter
                        )
                    }
                    item {
                        SettingCard(
                            title = AppStrings.SettingsAppExclusionsTitle,
                            description = AppStrings.SettingsAppExclusionsDesc,
                            isExpandable = true,
                            isExpanded = expandedSection == "permissions",
                            onHeaderClick = { expandedSection = if (expandedSection == "permissions") null else "permissions" }
                        ) {
                            Column(modifier = Modifier.padding(top = Dimensions.PaddingSmall)) {
                                state.availableApps.forEach { app ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .padding(vertical = Dimensions.Half),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(AppWeights.Full)) {
                                            Text(app.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                        Switch(checked = app.isExcluded, onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleExclusion(app.packageName)) })
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SettingCard(
                            title = AppStrings.SettingsDataRetentionTitle,
                            description = AppStrings.SettingsDataRetentionDesc.replace("%d", state.dataRetentionDays.toString()),
                            isExpandable = true,
                            isExpanded = expandedSection == "data",
                            onHeaderClick = { expandedSection = if (expandedSection == "data") null else "data" }
                        ) {
                            Column(modifier = Modifier.padding(top = Dimensions.PaddingSmall)) {
                                Text(AppStrings.SettingsDataRetentionSelect, style = MaterialTheme.typography.bodyMedium)
                                Slider(
                                    value = state.dataRetentionDays.toFloat(),
                                    onValueChange = { viewModel.onEvent(SettingsEvent.SetDataRetention(it.toInt())) },
                                    valueRange = 1f..365f,
                                    steps = 364
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

                    item { SettingCategory(AppStrings.SettingsCategoryAbout) }
                    item { 
                        InfoCard(
                            title = AppStrings.SettingsAboutTimelineTitle, 
                            description = AppStrings.SettingsAboutTimelineDesc, 
                            icon = Icons.Default.Info
                        ) 
                    }

                    item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

                    item { SettingCategory(AppStrings.SettingsCategorySupport) }
                    item { 
                        InfoCard(
                            title = AppStrings.SettingsContactUsTitle, 
                            description = AppStrings.SettingsContactUsDesc, 
                            icon = Icons.AutoMirrored.Filled.ContactSupport
                        ) 
                    }
                    item { 
                        InfoCard(
                            title = AppStrings.SettingsReportBugsTitle, 
                            description = AppStrings.SettingsReportBugsDesc, 
                            icon = Icons.Default.BugReport
                        ) 
                    }

                    item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.PaddingLarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Version ${AppStrings.SettingsAppVersionValue}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(Dimensions.Quat))
                            Text(
                                text = AppStrings.SettingsMadeByMo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = AppAlpha.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}
