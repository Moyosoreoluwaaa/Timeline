package com.timeline.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberEmailLauncher(): (to: String, subject: String, body: String) -> Unit