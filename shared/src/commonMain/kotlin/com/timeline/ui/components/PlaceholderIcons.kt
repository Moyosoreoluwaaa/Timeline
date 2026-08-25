package com.timeline.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.timeline.ui.theme.Dimensions

object PlaceholderIcons {
    @Composable
    fun GoogleIcon() {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.IconSmall),
            tint = Color.White
        )
    }

    @Composable
    fun AppleIcon() {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.IconSmall),
            tint = Color.White
        )
    }
}
