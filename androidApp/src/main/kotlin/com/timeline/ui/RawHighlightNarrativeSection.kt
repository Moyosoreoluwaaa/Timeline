package com.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timeline.presentation.RawHighlightState
import com.timeline.util.AppStrings

@Composable
internal fun AppDailyNarrativeSection(
    state: RawHighlightState,
    onOptInToggle: () -> Unit,
    onGenerate: () -> Unit,
    onCopy: (String) -> Unit
) {
    if (!state.isAgentOptedIn) {
        SmartOptInBanner(onOptIn = onOptInToggle)
        return
    }

    if (state.isReasoningLoading) {
        val stageText = when (state.reasoningStage) {
            RawHighlightState.ReasoningStage.REDACTING -> AppStrings.HighlightSanitizingPii
            RawHighlightState.ReasoningStage.DEDUPLICATING -> AppStrings.HighlightDeduplicating
            RawHighlightState.ReasoningStage.CONTEXTUALIZING -> AppStrings.HighlightContextualizing
            RawHighlightState.ReasoningStage.REASONING -> AppStrings.HighlightReasoning
            else -> AppStrings.HighlightAnalyzingUsage
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = AppStrings.HighlightGeminiEngine, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = stageText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(progress = { state.reasoningProgress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val selectedPackage = state.selectedPackage
    val reasoning = if (selectedPackage != null) {
        state.appDailyReasonings[selectedPackage]
    } else {
        state.appDailyReasonings["ALL_APPS"] ?: state.appDailyReasonings.values.firstOrNull()
    }

    val narrativeTitle = if (selectedPackage != null) {
        val appName = state.filteredScreenshots.firstOrNull()?.displayName ?: selectedPackage
        AppStrings.HighlightAgentNarrativeInsight.replace("%s", appName)
    } else {
        AppStrings.HighlightDailyNarrative
    }

    if (reasoning != null) {
        var showPrivacyInfo by remember { mutableStateOf(false) }

        if (showPrivacyInfo) {
            AlertDialog(
                onDismissRequest = { showPrivacyInfo = false },
                title = { Text(AppStrings.HighlightPrivacyShieldActive) },
                text = { Text(AppStrings.HighlightPrivacyShieldDesc) },
                confirmButton = { TextButton(onClick = { showPrivacyInfo = false }) { Text(AppStrings.HighlightGotIt) } }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = narrativeTitle, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))

                    if (state.isPrivacyShieldActive) {
                        Badge(
                            modifier = Modifier.padding(end = 6.dp).clickable { showPrivacyInfo = true },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Shield, contentDescription = AppStrings.HighlightPrivacyShieldActive, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = AppStrings.HighlightSanitized, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    val modeIcon = when (state.categoryIcon) {
                        "brain" -> Icons.Rounded.Psychology
                        "chat" -> Icons.Rounded.Forum
                        "people" -> Icons.Rounded.People
                        "terminal" -> Icons.Rounded.Terminal
                        "play" -> Icons.Rounded.PlayArrow
                        else -> Icons.Rounded.Info
                    }

                    Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(modeIcon, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = reasoning.cognitiveMode, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TypewriterText(text = reasoning.summary, shouldAnimate = true, onAnimationComplete = {}, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)

                if (reasoning.actionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = AppStrings.HighlightTasksNextSteps, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onCopy(reasoning.actionItems.joinToString("\n") { "• $it" }) }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = AppStrings.HighlightCopyTasks, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    reasoning.actionItems.forEach { item ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    } else {
        OutlinedButton(onClick = onGenerate, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (selectedPackage != null) AppStrings.HighlightGenerateSmartNarrativeApp else AppStrings.HighlightSynthesizeDailyNarrative)
        }
    }
}
