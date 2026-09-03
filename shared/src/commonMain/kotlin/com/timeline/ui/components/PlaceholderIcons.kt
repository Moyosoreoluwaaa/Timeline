package com.timeline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timeline.ui.theme.Dimensions
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.apple_logo
import timeline.shared.generated.resources.google_logo

object PlaceholderIcons {
    @Composable
    fun GoogleIcon() {
        Image(
            painter = painterResource(Res.drawable.google_logo),
            contentDescription = null,
            modifier = Modifier.size(Dimensions.IconSmall)
        )
    }

    @Composable
    fun AppleIcon() {
        Image(
            painter = painterResource(Res.drawable.apple_logo),
            contentDescription = null,
            modifier = Modifier.size(Dimensions.IconSmall)
        )
    }
}
