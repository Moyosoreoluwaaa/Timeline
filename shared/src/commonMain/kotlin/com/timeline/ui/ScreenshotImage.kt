package com.timeline.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ScreenshotImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
