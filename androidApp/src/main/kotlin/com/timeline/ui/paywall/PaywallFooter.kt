package com.timeline.ui.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
internal fun PaywallFooter(
    onRestorePurchases: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfUse: () -> Unit,
    uriHandler: UriHandler,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

    TextButton(
        onClick = onRestorePurchases,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = AppStrings.PaywallRestorePurchases,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Medium)
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            onPrivacyPolicy()
            uriHandler.openUri("https://timeline-records.web.app/privacy-policy.html")
        }) {
            Text(
                text = AppStrings.PaywallPrivacyPolicy,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Low)
            )
        }

        Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Low),
            modifier = Modifier.padding(horizontal = Dimensions.PaddingSmall)
        )

        TextButton(onClick = {
            onTermsOfUse()
            uriHandler.openUri("https://timeline-records.web.app/terms-of-service.html")
        }) {
            Text(
                text = AppStrings.PaywallTermsOfUse,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Low)
            )
        }
    }
}
