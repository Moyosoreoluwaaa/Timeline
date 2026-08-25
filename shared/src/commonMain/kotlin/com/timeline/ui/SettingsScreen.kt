package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.components.*
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppWeights
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToPaywall: (isDeals: Boolean) -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedSection by remember { mutableStateOf<String?>(null) }
    var isYearlySelected by remember { mutableStateOf(true) }

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
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimensions.PaddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
                    contentPadding = PaddingValues(
                        top = Dimensions.PaddingMedium,
                        bottom = navBarPadding + Dimensions.PaddingExtraLarge
                    )
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.SurfaceVariant))
                                .padding(Dimensions.PaddingMedium)
                                .clickable { onNavigateToPaywall(false) }
                        ) {
                            Text(
                                text = "You're a free user, get more",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                            
                            PaywallFeatures()
                            
                            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                            
                            PlanOption(
                                title = AppStrings.PaywallMonthlyOption,
                                subtitle = AppStrings.PaywallMonthlyBilling,
                                selected = !isYearlySelected,
                                onClick = { isYearlySelected = false }
                            )
                            
                            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))

                            PlanOption(
                                title = AppStrings.PaywallYearlyOption,
                                subtitle = AppStrings.PaywallYearlyBilling,
                                selected = isYearlySelected,
                                onClick = { isYearlySelected = true },
                                trailing = { PromoBadge(AppStrings.PaywallSave50) }
                            )

                            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                            Button(
                                onClick = { onNavigateToPaywall(isYearlySelected) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimensions.ButtonHeight),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = AppStrings.PaywallStartTrial,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        TrackingSettingsSection(
                            state = state,
                            onUsageTrackingChange = { viewModel.onEvent(SettingsEvent.SetUsageTracking(it)) },
                            onScreenshotCaptureChange = { viewModel.onEvent(SettingsEvent.SetScreenshotCapture(it)) },
                            onFloatingOverlayChange = { viewModel.onEvent(SettingsEvent.SetFloatingOverlay(it)) }
                        )
                    }

                    item { SettingCategory(AppStrings.SettingsCategoryPermissions) }
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

                    item { SettingCategory(AppStrings.SettingsCategoryData) }
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

                    item { SettingCategory(AppStrings.SettingsCategoryAbout) }
                    item { InfoCard(title = AppStrings.SettingsAboutTimelineTitle, description = AppStrings.SettingsAboutTimelineDesc, icon = Icons.Default.Info) }
                    item { InfoCard(title = AppStrings.SettingsAppVersionTitle, description = AppStrings.SettingsAppVersionValue, icon = Icons.Default.Info) }

                    item { SettingCategory(AppStrings.SettingsCategorySupport) }
                    item { InfoCard(title = AppStrings.SettingsContactUsTitle, description = AppStrings.SettingsContactUsDesc, icon = Icons.AutoMirrored.Filled.ContactSupport) }
                    item { InfoCard(title = AppStrings.SettingsReportBugsTitle, description = AppStrings.SettingsReportBugsDesc, icon = Icons.Default.BugReport) }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.SurfaceVariant)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(modifier = Modifier.padding(Dimensions.PaddingMedium), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(AppWeights.Full)) {
                                    Text(
                                        text = AppStrings.SettingsLightweightMsg,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = AppStrings.SettingsNoContinuousMsg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
