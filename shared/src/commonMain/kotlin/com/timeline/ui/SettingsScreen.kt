package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.ui.components.*
import com.timeline.ui.theme.*
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
    var showReasoningSheet by remember { mutableStateOf(false) }
    var showExclusionsSheet by remember { mutableStateOf(false) }
    var showRetentionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.onEvent(SettingsEvent.LoadSettings) }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.statusBars,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = topPadding).padding(horizontal = Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
            contentPadding = PaddingValues(top = Dimensions.PaddingMedium, bottom = navBarPadding + Dimensions.PaddingExtraLarge)
        ) {
            item { UpgradeCard(onUpgradeClick = { onNavigateToPaywall(false) }) }

            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

            item { SettingCategory(AppStrings.SettingsCategoryPreference) }
            item {
                SettingItem(
                    title = AppStrings.SettingsHighlightsReasoning,
                    icon = Icons.Outlined.AutoAwesome,
                    onClick = { showReasoningSheet = true }
                )
            }

            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

            item { SettingCategory(AppStrings.SettingsCategoryTracking) }
            item {
                SettingItem(
                    title = AppStrings.SettingsUsageTrackingTitle,
                    icon = Icons.Outlined.Analytics,
                    trailing = { Switch(checked = state.isUsageTrackingEnabled, onCheckedChange = { viewModel.onEvent(SettingsEvent.SetUsageTracking(it)) }) },
                    onClick = { viewModel.onEvent(SettingsEvent.SetUsageTracking(!state.isUsageTrackingEnabled)) }
                )
            }
            item {
                SettingItem(
                    title = AppStrings.SettingsScreenshotCaptureTitle,
                    icon = Icons.Outlined.Screenshot,
                    trailing = { Switch(checked = state.isScreenshotCaptureEnabled, onCheckedChange = { viewModel.onEvent(SettingsEvent.SetScreenshotCapture(it)) }) },
                    onClick = { viewModel.onEvent(SettingsEvent.SetScreenshotCapture(!state.isScreenshotCaptureEnabled)) }
                )
            }

            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

            item { SettingCategory(AppStrings.SettingsCategoryData) }
            item {
                SettingItem(
                    title = AppStrings.SettingsManageSubscription,
                    icon = Icons.Outlined.CreditCard,
                    onClick = onNavigateToCustomerCenter
                )
            }
            item {
                SettingItem(
                    title = AppStrings.SettingsAppExclusionsTitle,
                    icon = Icons.Outlined.Block,
                    onClick = { showExclusionsSheet = true }
                )
            }
            item {
                SettingItem(
                    title = AppStrings.SettingsDataRetentionTitle,
                    icon = Icons.Outlined.History,
                    onClick = { showRetentionSheet = true }
                )
            }

            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }
            
            item { SettingCategory(AppStrings.SettingsCategorySupport) }
            item {
                InfoCard(
                    title = AppStrings.SettingsContactUsTitle,
                    icon = Icons.Outlined.SupportAgent,
                    onClick = {}
                )
            }
            item {
                InfoCard(
                    title = AppStrings.SettingsReportBugsTitle,
                    icon = Icons.Outlined.BugReport,
                    onClick = {}
                )
            }
            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.PaddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AppStrings.CommonVersion.replace("%s", AppStrings.SettingsAppVersionValue),
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

    if (showReasoningSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReasoningSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                Text(AppStrings.SettingsReasoningMode, style = MaterialTheme.typography.titleLarge)
                com.timeline.domain.reasoning.HighlightReasoningMode.entries.forEach { mode ->
                    ListItem(
                        headlineContent = { Text(mode.displayName) },
                        trailingContent = { RadioButton(selected = state.highlightReasoningMode == mode, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.clickable { viewModel.onEvent(SettingsEvent.SetReasoningMode(mode)) }
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                Text(AppStrings.SettingsFrequencyLabel.replace("%d", state.digestFrequency.toString()), style = MaterialTheme.typography.titleLarge)
                ValueSlider(
                    value = state.digestFrequency.toFloat(),
                    onValueChange = { viewModel.onEvent(SettingsEvent.SetDigestFrequency(it.toInt())) },
                    valueRange = 1f..6f,
                    steps = 5,
                    label = { AppStrings.SettingsFrequencyLabel.replace("%d", it.toInt().toString()) }
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            }
        }
    }

    if (showExclusionsSheet) {
        val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro
        ModalBottomSheet(
            onDismissRequest = { showExclusionsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                item { Text(AppStrings.SettingsAppExclusionsTitle, style = MaterialTheme.typography.titleLarge) }
                if (isFeatureLocked) {
                    item { Button(onClick = { onNavigateToPaywall(false) }) { Text(AppStrings.SettingsUnlockPro) } }
                } else {
                    items(state.availableApps) { app ->
                        ListItem(
                            headlineContent = { Text(app.name) },
                            leadingContent = {
                                AppIcon(icon = app.icon, contentDescription = app.name, modifier = Modifier.size(Dimensions.IconMedium))
                            },
                            trailingContent = { Switch(checked = app.isExcluded, onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleExclusion(app.packageName)) }) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }
        }
    }

    if (showRetentionSheet) {
        val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro
        ModalBottomSheet(
            onDismissRequest = { showRetentionSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                Text(AppStrings.SettingsDataRetentionTitle, style = MaterialTheme.typography.titleLarge)
                ValueSlider(
                    value = state.dataRetentionDays.toFloat(),
                    onValueChange = {
                        val newValue = if (isFeatureLocked) it.toInt().coerceAtMost(7) else it.toInt()
                        viewModel.onEvent(SettingsEvent.SetDataRetention(newValue))
                    },
                    valueRange = 1f..if (isFeatureLocked) 7f else 365f,
                    steps = if (isFeatureLocked) 6 else 364,
                    label = { AppStrings.SettingsDaysLabel.replace("%d", it.toInt().toString()) }
                )
            }
        }
    }
}
