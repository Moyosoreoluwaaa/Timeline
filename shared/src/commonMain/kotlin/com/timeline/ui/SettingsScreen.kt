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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.components.InfoCard
import com.timeline.ui.components.SettingCard
import com.timeline.ui.components.SettingCategory
import com.timeline.ui.components.SettingsTopBar
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.ui.components.UpgradeCard
import androidx.compose.ui.text.style.TextOverflow
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
    onNavigateToAuth: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedSection by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.onEvent(SettingsEvent.LoadSettings) }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SettingsTopBar(
                isLoggedIn = state.isLoggedIn,
                onBack = onBack,
                onNavigateToAuth = onNavigateToAuth,
                onLogout = { viewModel.onEvent(SettingsEvent.Logout) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val topPadding = (padding.calculateTopPadding() - TopAppBarCutoutRadius).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .padding(top = topPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
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

                    item { SettingCategory("Highlight Preferences") }
                    item {
                        SettingCard(
                            title = "Reasoning Mode",
                            description = "Choose AI depth",
                            status = state.highlightReasoningMode.displayName,
                            isExpandable = true,
                            isExpanded = expandedSection == "reasoning_mode",
                            onHeaderClick = { expandedSection = if (expandedSection == "reasoning_mode") null else "reasoning_mode" }
                        ) {
                            Column(modifier = Modifier.padding(top = Dimensions.PaddingSmall), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                com.timeline.domain.reasoning.HighlightReasoningMode.entries.forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable {
                                                viewModel.onEvent(SettingsEvent.SetReasoningMode(mode))
                                                expandedSection = null
                                            }
                                            .padding(vertical = Dimensions.Half, horizontal = Dimensions.PaddingSmall),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = mode.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (state.highlightReasoningMode == mode) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (state.highlightReasoningMode == mode) {
                                            RadioButton(selected = true, onClick = null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SettingCard(
                            title = "Highlight Schedule & Frequency",
                            description = "Configure delivery times",
                            status = "${state.digestFrequency}x daily",
                            isExpandable = true,
                            isExpanded = expandedSection == "digest_schedule",
                            onHeaderClick = { expandedSection = if (expandedSection == "digest_schedule") null else "digest_schedule" }
                        ) {
                            Column(modifier = Modifier.padding(top = Dimensions.PaddingSmall), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Digest Frequency (${state.digestFrequency} times daily)", style = MaterialTheme.typography.bodyMedium)
                                Slider(
                                    value = state.digestFrequency.toFloat(),
                                    onValueChange = { viewModel.onEvent(SettingsEvent.SetDigestFrequency(it.toInt())) },
                                    valueRange = 1f..6f,
                                    steps = 5
                                )
                            }
                        }
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
                            title = "Manage your active subscription",
                            description = "View and manage your active plans.",
                            onClick = onNavigateToCustomerCenter
                        )
                    }
                    item {
                        val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro
                        SettingCard(
                            title = AppStrings.SettingsAppExclusionsTitle,
                            description = if (isFeatureLocked) "Unlock with Trial or Pro" else AppStrings.SettingsAppExclusionsDesc,
                            isExpandable = !isFeatureLocked,
                            isExpanded = expandedSection == "permissions" && !isFeatureLocked,
                            onHeaderClick = {
                                if (isFeatureLocked) onNavigateToPaywall(false)
                                else expandedSection = if (expandedSection == "permissions") null else "permissions"
                            },
                            trailing = {
                                if (isFeatureLocked) Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        ) {
                            Column(modifier = Modifier.padding(top = Dimensions.PaddingSmall)) {
                                state.availableApps.forEach { app ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .padding(vertical = Dimensions.Half),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(Dimensions.IconMedium),
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            AppIcon(
                                                icon = app.icon,
                                                contentDescription = app.name,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
                                        Text(
                                            text = app.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(AppWeights.Full),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Switch(
                                            checked = app.isExcluded,
                                            onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleExclusion(app.packageName)) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro
                        SettingCard(
                            title = AppStrings.SettingsDataRetentionTitle,
                            description = if (isFeatureLocked) "Up to 7 days (Locked)" else AppStrings.SettingsDataRetentionDesc.replace("%d", state.dataRetentionDays.toString()),
                            isExpandable = true,
                            isExpanded = expandedSection == "data",
                            onHeaderClick = { expandedSection = if (expandedSection == "data") null else "data" },
                            trailing = {
                                if (isFeatureLocked) Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        ) {
                            Column(modifier = Modifier.padding(top = Dimensions.PaddingSmall)) {
                                Text(
                                    if (isFeatureLocked) "Free users are capped at 7 days. Upgrade to extend."
                                    else AppStrings.SettingsDataRetentionSelect,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    value = state.dataRetentionDays.toFloat(),
                                    onValueChange = {
                                        val newValue = if (isFeatureLocked) it.toInt().coerceAtMost(7) else it.toInt()
                                        viewModel.onEvent(SettingsEvent.SetDataRetention(newValue))
                                    },
                                    valueRange = 1f..if (isFeatureLocked) 7f else 365f,
                                    steps = if (isFeatureLocked) 6 else 364,
                                    enabled = !isFeatureLocked || state.dataRetentionDays > 1 // Allow reducing but not increasing beyond 7 if locked
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
                            icon = Icons.Rounded.Info
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
                            icon = Icons.Rounded.BugReport
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