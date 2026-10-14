package com.timeline.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timeline.presentation.RawHighlightEvent
import com.timeline.presentation.RawHighlightState
import com.timeline.presentation.RawHighlightViewModel
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RawHighlightTopBar(
    state: RawHighlightState,
    viewModel: RawHighlightViewModel,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = AppStrings.HighlightTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = AppStrings.ContentDescBack,
                    modifier = Modifier.size(Dimensions.IconSmall)
                )
            }
        },
        actions = {
            val isActive = state.isAgentOptedIn
            val penTint = if (isActive) Color(0xFFFF8C00) else MaterialTheme.colorScheme.onSurfaceVariant
            IconButton(
                onClick = { viewModel.onEvent(RawHighlightEvent.ToggleAgentOptIn) },
                modifier = Modifier.testTag("highlight_pen_button")
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Rounded.Star else Icons.Rounded.Edit,
                    contentDescription = if (isActive) AppStrings.HighlightSmartActive else AppStrings.HighlightPenToggle,
                    tint = penTint,
                    modifier = Modifier.size(Dimensions.IconSmall)
                )
            }
            IconButton(
                onClick = { viewModel.onEvent(RawHighlightEvent.SyncRealData) },
                modifier = Modifier.testTag("highlight_sync_real_button"),
                enabled = !state.isSyncingDeviceUsage
            ) {
                if (state.isSyncingDeviceUsage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.IconSmall),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = AppStrings.RealDataSyncButton,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.IconSmall)
                    )
                }
            }
            IconButton(onClick = { onShowDatePickerChange(true) }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = AppStrings.HighlightPickDate,
                    modifier = Modifier.size(Dimensions.IconSmall)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { onShowDatePickerChange(false) },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    viewModel.onEvent(RawHighlightEvent.SelectDate(millis?.let { Instant.fromEpochMilliseconds(it) }))
                    onShowDatePickerChange(false)
                }) {
                    Text(AppStrings.TimelineOk)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDatePickerChange(false) }) {
                    Text(AppStrings.TimelineCancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
