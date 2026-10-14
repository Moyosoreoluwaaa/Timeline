package com.timeline.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun TypewriterText(
    text: String,
    shouldAnimate: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (!shouldAnimate) {
        Text(text = text, style = style, fontFamily = fontFamily, color = color, modifier = modifier)
        return
    }

    var displayedLength by remember(text) { mutableIntStateOf(0) }
    var isAnimationComplete by remember(text) { mutableStateOf(false) }

    LaunchedEffect(text) {
        displayedLength = 0
        isAnimationComplete = false
        if (text.isEmpty()) {
            isAnimationComplete = true
            onAnimationComplete()
            return@LaunchedEffect
        }
        val totalChars = text.length
        val step = when {
            totalChars > 600 -> 6
            totalChars > 300 -> 4
            totalChars > 150 -> 2
            else -> 1
        }
        var current = 0
        while (current < totalChars) {
            current = (current + step).coerceAtMost(totalChars)
            displayedLength = current
            delay(10)
        }
        isAnimationComplete = true
        onAnimationComplete()
    }

    val visibleText = remember(displayedLength, text) { text.take(displayedLength) }

    Text(
        text = if (!isAnimationComplete) "$visibleText ▌" else visibleText,
        style = style,
        fontFamily = fontFamily,
        color = color,
        modifier = modifier.clickable {
            displayedLength = text.length
            isAnimationComplete = true
            onAnimationComplete()
        }
    )
}

@Composable
internal fun EntityRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
