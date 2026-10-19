package com.timeline.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsState
import com.timeline.presentation.SettingsViewModel
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.InfoCard
import com.timeline.ui.components.SettingCategory
import com.timeline.ui.components.SettingItem
import com.timeline.ui.components.UpgradeCard
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
internal fun SettingsListContent(
    state: SettingsState,
    viewModel: SettingsViewModel,
    tutorialState: com.timeline.tutorial.TutorialState,
    tutorialViewModel: TutorialViewModel,
    scrollState: LazyListState,
    topPadding: Dp,
    navBarPadding: Dp,
    onNavigateToPaywall: (Boolean) -> Unit,
    onNavigateToCustomerCenter: () -> Unit,
    onOpenReasoningSheet: () -> Unit,
    onOpenExclusionsSheet: () -> Unit,
    onOpenRetentionSheet: () -> Unit,
    onOpenBugReportSheet: () -> Unit,
    onContactUs: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier
            .fillMaxSize()
            .padding(top = topPadding)
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
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                    }
                ),
                onClick = {
                    onOpenReasoningSheet()
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
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
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
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                    }
                ),
                onClick = {
                    if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_APP_EXCLUSIONS) {
                        // NextStep → SPOTLIGHT_EXCLUSIONS_SHEET; SettingsScreen.LaunchedEffect opens the sheet
                        tutorialViewModel.onEvent(TutorialEvent.NextStep)
                    } else {
                        onOpenExclusionsSheet()
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
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                    }
                ),
                onClick = {
                    if (tutorialState.currentStep == TutorialStep.SPOTLIGHT_DATA_RETENTION) {
                        // NextStep → SPOTLIGHT_RETENTION_SHEET; SettingsScreen.LaunchedEffect opens the sheet
                        tutorialViewModel.onEvent(TutorialEvent.NextStep)
                    } else {
                        onOpenRetentionSheet()
                    }
                }
            )
        }


        item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }

        item { SettingCategory(AppStrings.SettingsCategorySupport) }
        item { InfoCard(title = AppStrings.SettingsContactUsTitle, icon = Icons.Outlined.SupportAgent, onClick = onContactUs) }
        item { InfoCard(title = AppStrings.SettingsReportBugsTitle, icon = Icons.Outlined.BugReport, onClick = onOpenBugReportSheet) }
        item { Spacer(modifier = Modifier.height(Dimensions.PaddingMedium)) }
        item { SettingsFooter(version = state.appVersion) }
    }
}
