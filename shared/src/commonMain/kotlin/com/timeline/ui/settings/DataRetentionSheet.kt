package com.timeline.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.SettingsState
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.ValueSlider
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DataRetentionSheet(
    state: SettingsState,
    tutorialStep: TutorialStep,
    tutorialViewModel: TutorialViewModel,
    onDismiss: () -> Unit,
    onRetentionChange: (Int) -> Unit
) {
    val isFeatureLocked = state.trialStatus != TrialStatus.ACTIVE && !state.isPro

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            if (tutorialStep == TutorialStep.SPOTLIGHT_RETENTION_SHEET) {
                tutorialViewModel.onEvent(TutorialEvent.NextStep)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(Dimensions.PaddingMedium)
                .spotlightTarget(
                    TutorialStep.SPOTLIGHT_RETENTION_SHEET,
                    onBoundsCalculated = { step, bounds ->
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                    }
                )
        ) {
            Text(AppStrings.SettingsDataRetentionTitle, style = MaterialTheme.typography.titleLarge)
            ValueSlider(
                value = state.dataRetentionDays.toFloat(),
                onValueChange = {
                    val newValue = if (isFeatureLocked) it.toInt().coerceAtMost(7) else it.toInt()
                    onRetentionChange(newValue)
                    if (tutorialStep == TutorialStep.SPOTLIGHT_RETENTION_SHEET) {
                        tutorialViewModel.onEvent(TutorialEvent.NextStep)
                    }
                },
                valueRange = 1f..if (isFeatureLocked) 7f else 60f,
                steps = if (isFeatureLocked) 6 else 60,
                label = {
                    AppStrings.SettingsDaysLabel.replace("%d", it.toInt().toString())
                }
            )
        }
    }
}
