package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.*
import com.timeline.ui.theme.*
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    tutorialViewModel: TutorialViewModel = org.koin.compose.viewmodel.koinViewModel(),
    onNavigateToPaywall: (isDeals: Boolean) -> Unit = {},
    onNavigateToCustomerCenter: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
    var showReasoningSheet by remember { mutableStateOf(false) }
    var showExclusionsSheet by remember { mutableStateOf(false) }
    var showRetentionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(tutorialState.currentStep) {
        when (tutorialState.currentStep) {
            TutorialStep.SPOTLIGHT_HIGHLIGHTS_PREFERENCE -> {
                showReasoningSheet = false
                scrollState.animateScrollToItem(0)
            }

            TutorialStep.SPOTLIGHT_REASONING_BALANCED -> {
                showReasoningSheet = true
            }

            TutorialStep.SPOTLIGHT_REASONING_SLIDER -> {
                showReasoningSheet = true
            }

            TutorialStep.SPOTLIGHT_TRACKING_OPTIONS -> {
                showReasoningSheet = false
                scrollState.animateScrollToItem(3)
            }

            TutorialStep.SPOTLIGHT_APP_EXCLUSIONS -> {
                showExclusionsSheet = false
                scrollState.animateScrollToItem(8)
            }

            TutorialStep.SPOTLIGHT_EXCLUSION_MOCK_ITEM -> {
                showExclusionsSheet = true
            }

            TutorialStep.SPOTLIGHT_DATA_RETENTION -> {
                showExclusionsSheet = false
                showRetentionSheet = false
                scrollState.animateScrollToItem(9)
            }

            TutorialStep.SPOTLIGHT_RETENTION_SHEET_CONTENT -> {
                showRetentionSheet = true
            }

            else -> {
                if (tutorialState.isActive) {
                    showReasoningSheet = false
                    showExclusionsSheet = false
                    showRetentionSheet = false
                }
            }
        }
    }

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
            state = scrollState,
            modifier = Modifier.fillMaxSize().padding(top = topPadding)
                .padding(horizontal = Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
            contentPadding = PaddingValues(
                top = Dimensions.PaddingMedium,
                bottom = navBarPadding + Dimensions.PaddingExtraLarge
            )
        ) {
            item { UpgradeCard(onUpgradeClick = { onNavigateToPaywall(false) }) }

            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

            item { SettingCategory(AppStrings.SettingsCategoryPreference) }
            item {
                SettingItem(
                    title = AppStrings.SettingsHighlightsReasoning,
                    icon = Icons.Outlined.AutoAwesome,
                    modifier = Modifier.spotlightTarget(
                        TutorialStep.SPOTLIGHT_HIGHLIGHTS_PREFERENCE,
                        onBoundsCalculated = { step, bounds ->
                            tutorialViewModel.onEvent(
                                TutorialEvent.UpdateTargetBounds(
                                    step,
                                    bounds
                                )
                            )
                        }
                    ),
                    onClick = {
                        showReasoningSheet = true
                        if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_HIGHLIGHTS_PREFERENCE) {
                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

            item { SettingCategory(AppStrings.SettingsCategoryTracking) }
            item {
                SettingItem(
                    title = AppStrings.SettingsUsageTrackingTitle,
                    icon = Icons.Outlined.Analytics,
                    trailing = {
                        Switch(checked = state.isUsageTrackingEnabled, onCheckedChange = {
                            viewModel.onEvent(SettingsEvent.SetUsageTracking(it))
                            if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_TRACKING_OPTIONS) {
                                tutorialViewModel.onEvent(TutorialEvent.NextStep)
                            }
                        })
                    },
                    modifier = Modifier.spotlightTarget(
                        TutorialStep.SPOTLIGHT_TRACKING_OPTIONS,
                        onBoundsCalculated = { step, bounds ->
                            tutorialViewModel.onEvent(
                                TutorialEvent.UpdateTargetBounds(
                                    step,
                                    bounds
                                )
                            )
                        }
                    ),
                    onClick = {
                        viewModel.onEvent(SettingsEvent.SetUsageTracking(!state.isUsageTrackingEnabled))
                        if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_TRACKING_OPTIONS) {
                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                        }
                    }
                )
            }
            item {
                SettingItem(
                    title = AppStrings.SettingsScreenshotCaptureTitle,
                    icon = Icons.Outlined.Screenshot,
                    trailing = {
                        Switch(checked = state.isScreenshotCaptureEnabled, onCheckedChange = {
                            viewModel.onEvent(SettingsEvent.SetScreenshotCapture(it))
                            if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_TRACKING_OPTIONS) {
                                tutorialViewModel.onEvent(TutorialEvent.NextStep)
                            }
                        })
                    },
                    onClick = {
                        viewModel.onEvent(SettingsEvent.SetScreenshotCapture(!state.isScreenshotCaptureEnabled))
                        if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_TRACKING_OPTIONS) {
                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                        }
                    }
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
                    modifier = Modifier.spotlightTarget(
                        TutorialStep.SPOTLIGHT_APP_EXCLUSIONS,
                        onBoundsCalculated = { step, bounds ->
                            tutorialViewModel.onEvent(
                                TutorialEvent.UpdateTargetBounds(
                                    step,
                                    bounds
                                )
                            )
                        }
                    ),
                    onClick = {
                        showExclusionsSheet = true
                        if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_APP_EXCLUSIONS) {
                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                        }
                    }
                )
            }
            item {
                SettingItem(
                    title = AppStrings.SettingsDataRetentionTitle,
                    icon = Icons.Outlined.History,
                    modifier = Modifier.spotlightTarget(
                        TutorialStep.SPOTLIGHT_DATA_RETENTION,
                        onBoundsCalculated = { step, bounds ->
                            tutorialViewModel.onEvent(
                                TutorialEvent.UpdateTargetBounds(
                                    step,
                                    bounds
                                )
                            )
                        }
                    ),
                    onClick = {
                        showRetentionSheet = true
                        if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_DATA_RETENTION) {
                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                        }
                    }
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
                        text = AppStrings.CommonVersion.replace(
                            "%s",
                            AppStrings.SettingsAppVersionValue
                        ),
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
                        trailingContent = {
                            RadioButton(
                                selected = state.highlightReasoningMode == mode,
                                onClick = null
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .clickable { viewModel.onEvent(SettingsEvent.SetReasoningMode(mode)) }
                            .then(
                                if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_REASONING_BALANCED &&
                                    mode == com.timeline.domain.reasoning.HighlightReasoningMode.BALANCED
                                ) {
                                    Modifier.spotlightTarget(
                                        TutorialStep.SPOTLIGHT_REASONING_BALANCED,
                                        onBoundsCalculated = { step, bounds ->
                                            tutorialViewModel.onEvent(
                                                TutorialEvent.UpdateTargetBounds(
                                                    step,
                                                    bounds
                                                )
                                            )
                                        }
                                    )
                                } else Modifier)
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                Column(
                    modifier = Modifier.spotlightTarget(
                        TutorialStep.SPOTLIGHT_REASONING_SLIDER,
                        onBoundsCalculated = { step, bounds ->
                            tutorialViewModel.onEvent(
                                TutorialEvent.UpdateTargetBounds(
                                    step,
                                    bounds
                                )
                            )
                        }
                    )
                ) {
                    Text(
                        AppStrings.SettingsFrequencyLabel.replace(
                            "%d",
                            state.digestFrequency.toString()
                        ), style = MaterialTheme.typography.titleLarge
                    )
                    ValueSlider(
                        value = state.digestFrequency.toFloat(),
                        onValueChange = { viewModel.onEvent(SettingsEvent.SetDigestFrequency(it.toInt())) },
                        valueRange = 1f..6f,
                        steps = 5,
                        label = { "" },
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            }
        }
    }

    if (showExclusionsSheet) {
        val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro
        val mockApps = listOf(
            com.timeline.presentation.AppInfo("com.whatsapp", "WhatsApp", null, false),
            com.timeline.presentation.AppInfo(
                "com.google.android.apps.messaging",
                "Messages",
                null,
                false
            ),
            com.timeline.presentation.AppInfo("com.android.settings", "Settings", null, true)
        )
        val appsToDisplay = if (tutorialState.isActive) mockApps else state.availableApps

        ModalBottomSheet(
            onDismissRequest = { showExclusionsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                item {
                    Text(
                        AppStrings.SettingsAppExclusionsTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (isFeatureLocked && !tutorialState.isActive) {
                    item { Button(onClick = { onNavigateToPaywall(false) }) { Text(AppStrings.SettingsUnlockPro) } }
                } else {
                    items(appsToDisplay) { app ->
                        ListItem(
                            headlineContent = { Text(app.name) },
                            leadingContent = {
                                AppIcon(
                                    icon = app.icon,
                                    contentDescription = app.name,
                                    modifier = Modifier.size(Dimensions.IconMedium)
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = app.isExcluded,
                                    onCheckedChange = {
                                        viewModel.onEvent(
                                            SettingsEvent.ToggleExclusion(app.packageName)
                                        )
                                    })
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = if (tutorialState.isActive && app.packageName == "com.whatsapp") {
                                Modifier.spotlightTarget(
                                    TutorialStep.SPOTLIGHT_EXCLUSION_MOCK_ITEM,
                                    onBoundsCalculated = { step, bounds ->
                                        tutorialViewModel.onEvent(
                                            TutorialEvent.UpdateTargetBounds(
                                                step,
                                                bounds
                                            )
                                        )
                                    }
                                )
                            } else Modifier
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
            Column(
                modifier = Modifier
                    .padding(Dimensions.PaddingMedium)
                    .spotlightTarget(
                        TutorialStep.SPOTLIGHT_RETENTION_SHEET_CONTENT,
                        onBoundsCalculated = { step, bounds ->
                            tutorialViewModel.onEvent(
                                TutorialEvent.UpdateTargetBounds(
                                    step,
                                    bounds
                                )
                            )
                        }
                    )
            ) {
                Text(
                    AppStrings.SettingsDataRetentionTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                ValueSlider(
                    value = state.dataRetentionDays.toFloat(),
                    onValueChange = {
                        val newValue =
                            if (isFeatureLocked) it.toInt().coerceAtMost(7) else it.toInt()
                        viewModel.onEvent(SettingsEvent.SetDataRetention(newValue))
                    },
                    valueRange = 1f..if (isFeatureLocked) 7f else 60f,
                    steps = if (isFeatureLocked) 6 else 60,
                    label = { AppStrings.SettingsDaysLabel.replace("%d", it.toInt().toString()) }
                )
            }
        }
    }
}
