package com.timeline.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timeline.domain.reasoning.HighlightReasoningMode
import com.timeline.tutorial.TutorialEvent
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.tutorial.spotlightTarget
import com.timeline.ui.components.ValueSlider
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReasoningModeSheet(
    selectedMode: HighlightReasoningMode,
    digestFrequency: Int,
    tutorialStep: TutorialStep,
    tutorialViewModel: TutorialViewModel,
    onDismiss: () -> Unit,
    onSelectMode: (HighlightReasoningMode) -> Unit,
    onFrequencyChange: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            if (tutorialStep == TutorialStep.SPOTLIGHT_REASONING_SHEET) {
                tutorialViewModel.onEvent(TutorialEvent.NextStep)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(Dimensions.PaddingMedium)
                .spotlightTarget(
                    TutorialStep.SPOTLIGHT_REASONING_SHEET,
                    onBoundsCalculated = { step, bounds ->
                        tutorialViewModel.onEvent(TutorialEvent.UpdateTargetBounds(step, bounds))
                    }
                )
        ) {
            Text(AppStrings.SettingsReasoningMode, style = MaterialTheme.typography.titleLarge)
            HighlightReasoningMode.entries.forEach { mode ->
                ListItem(
                    headlineContent = { Text(mode.displayName) },
                    trailingContent = {
                        RadioButton(
                            selected = selectedMode == mode,
                            onClick = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        onSelectMode(mode)
                        if (tutorialStep == TutorialStep.SPOTLIGHT_REASONING_SHEET) {
                            tutorialViewModel.onEvent(TutorialEvent.NextStep)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            Column {
                Text(
                    AppStrings.SettingsFrequencyLabel.replace("%d", digestFrequency.toString()),
                    style = MaterialTheme.typography.titleLarge
                )
                ValueSlider(
                    value = digestFrequency.toFloat(),
                    onValueChange = { onFrequencyChange(it.toInt()) },
                    valueRange = 1f..6f,
                    steps = 5,
                    label = { "" }
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        }
    }
}
