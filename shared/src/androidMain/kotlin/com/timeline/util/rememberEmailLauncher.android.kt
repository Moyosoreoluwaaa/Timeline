package com.timeline.util

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberEmailLauncher(): (to: String, subject: String, body: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { to, subject, body ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$to")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                if (body.isNotEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, body)
                }
            }
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        }
    }
}