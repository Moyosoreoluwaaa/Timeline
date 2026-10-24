package com.timeline.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BugReportSheet(
    onDismiss: () -> Unit,
    onSubmit: (description: String) -> Unit
) {
    var bugDescription by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium)
        ) {
            Text(
                text = AppStrings.SettingsReportBugsTitle,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            OutlinedTextField(
                value = bugDescription,
                onValueChange = { bugDescription = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Describe the bug") },
                minLines = 3
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            Button(
                onClick = {
                    onDismiss()
                    onSubmit(bugDescription)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Bug Report")
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
        }
    }
}
