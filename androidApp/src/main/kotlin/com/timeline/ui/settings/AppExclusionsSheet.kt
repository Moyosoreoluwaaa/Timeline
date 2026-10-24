package com.timeline.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.AppInfo
import com.timeline.presentation.SettingsState
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.AppIcon
import com.timeline.ui.rememberAppIcon
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppExclusionsSheet(
    state: SettingsState,
    isTutorialActive: Boolean,
    tutorialStep: TutorialStep,
    tutorialViewModel: TutorialViewModel,
    onDismiss: () -> Unit,
    onToggleExclusion: (String) -> Unit,
    onNavigateToPaywall: (Boolean) -> Unit
) {
    val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro
    val timelineIcon = rememberAppIcon("com.timeline_records")
    val mockApps = listOf(
        AppInfo("com.timeline_records", "Timeline", timelineIcon, false),
        AppInfo(
            "com.google.android.youtube",
            "YouTube",
            rememberAppIcon("com.google.android.youtube"),
            false
        ),
        AppInfo("com.google.android.gm", "Gmail", rememberAppIcon("com.google.android.gm"), true)
    )
    val appsToDisplay = if (isTutorialActive) mockApps else state.availableApps

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            if (tutorialStep == TutorialStep.SPOTLIGHT_EXCLUSIONS_SHEET) {
                tutorialViewModel.onEvent(TutorialEvent.NextStep)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.PaddingMedium)
                .spotlightTarget(
                    TutorialStep.SPOTLIGHT_EXCLUSIONS_SHEET,
                    onBoundsCalculated = { step, bounds ->
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                    }
                )
        ) {
            item {
                Text(
                    AppStrings.SettingsAppExclusionsTitle,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (isFeatureLocked && !isTutorialActive) {
                item {
                    Column(
                        modifier = Modifier.height(100.dp).fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = { onNavigateToPaywall(false) },
                            modifier = Modifier
                                .padding(top = Dimensions.PaddingSmall)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(AppStrings.SettingsUnlockPro)
                        }
                    }
                }
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
                                    onToggleExclusion(app.packageName)
                                    if (tutorialStep == TutorialStep.SPOTLIGHT_EXCLUSIONS_SHEET) {
                                        tutorialViewModel.onEvent(TutorialEvent.NextStep)
                                    }
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}
