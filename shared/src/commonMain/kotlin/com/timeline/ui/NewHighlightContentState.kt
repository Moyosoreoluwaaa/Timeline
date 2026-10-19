package com.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.plusJakartaSansFamily
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun NewHighlightContentState(text: String) {
    val words = remember(text) { text.split(" ") }
    var visibleWordCount by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleWordCount = 0
        delay(200.milliseconds)
        while (visibleWordCount < words.size) {
            visibleWordCount++
            delay(80.milliseconds)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val displayedText = remember(visibleWordCount, words) {
            words.take(visibleWordCount).joinToString(" ")
        }

        val family = plusJakartaSansFamily()
        val narrativeStyle = TextStyle(
            fontFamily = family,
            fontSize = 25.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.Normal,
            fontFeatureSettings = "tnum",
            color = MaterialTheme.colorScheme.onSurface
        )

        val annotatedNarrative = remember(displayedText) {
            buildAnnotatedNarrative(displayedText)
        }

        AnimatedVisibility(
            visible = visibleWordCount > 0,
            enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500))
        ) {
            Text(
                text = annotatedNarrative,
                style = narrativeStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Formats the narrative by highlighting known app names in brand orange
 * and styling numeric metrics and times with tabular accents.
 */
private fun buildAnnotatedNarrative(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        append(rawText)
        
        // Match numbers, durations, and time windows (e.g., "42 minutes", "12 PM", "14 times", "35s")
        val metricRegex = Regex("""\b(\d+[\w:]*(?:\s*(?:AM|PM|am|pm|minutes|mins|hours|times|s|m))?)\b""")
        metricRegex.findAll(rawText).forEach { match ->
            addStyle(
                style = SpanStyle(
                    color = AppColors.BrandYellow,
                    fontWeight = FontWeight.Medium
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // Match typical app names or quoted/bracketed titles
        val appNames = listOf(
            "YouTube", "Timeline", "X", "WhatsApp", "Chrome", "Instagram",
            "TikTok", "Figma", "Slack", "Gmail", "Camera", "Spotify", "Reddit"
        )
        appNames.forEach { appName ->
            var startIndex = 0
            while (startIndex < rawText.length) {
                val index = rawText.indexOf(appName, startIndex, ignoreCase = false)
                if (index == -1) break
                addStyle(
                    style = SpanStyle(
                        color = AppColors.BrandOrange,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = index,
                    end = index + appName.length
                )
                startIndex = index + appName.length
            }
        }
    }
}
