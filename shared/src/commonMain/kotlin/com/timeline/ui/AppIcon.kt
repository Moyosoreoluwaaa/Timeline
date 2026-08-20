package com.timeline.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AppIcon(
    icon: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
