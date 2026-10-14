package com.timeline.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
internal fun SettingsFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = AppStrings.CommonVersion.replace("%s", AppStrings.SettingsAppVersionValue),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(Dimensions.Quat))
        Text(
            text = AppStrings.SettingsMadeByMo,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
