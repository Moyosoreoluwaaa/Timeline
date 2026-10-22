package com.timeline.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale


@Composable
actual fun ScreenshotImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    fullResolution: Boolean
) {
}


actual fun prefetchScreenshots(paths: List<String>) {
}