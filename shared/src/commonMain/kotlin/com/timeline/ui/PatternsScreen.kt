package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.MetricsViewModel
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PatternsScreenContent(scrollState: LazyListState) {
    val viewModel: MetricsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium),
        contentPadding = PaddingValues(top = Dimensions.PaddingMedium, bottom = Dimensions.SpacingMega)
    ) {
        item {
            Column {
                Text(
                    text = AppStrings.InsightsPatterns.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Dimensions.Half))
                Text(
                    text = AppStrings.InsightsFoundPatterns,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            PatternCard(
                title = AppStrings.InsightsPeakAtNight,
                description = AppStrings.InsightsMostActiveHour.replace("%d", state.peakActiveHour.toString()),
                icon = Icons.Rounded.Bedtime
            )
        }

        item {
            PatternCard(
                title = AppStrings.InsightsLongSessions.replace("%s", state.longestSessionAppName),
                description = AppStrings.InsightsLongestSessionWas.replace("%s", state.longestSessionTime),
                icon = Icons.Rounded.PlayArrow
            )
        }

        item {
            PatternCard(
                title = AppStrings.InsightsHeaviestDays.replace("%s", state.heaviestDayName),
                description = AppStrings.InsightsHeaviestDayUsage.replace("%s", state.heaviestDayUsageTime),
                icon = Icons.Rounded.CalendarMonth
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.PaddingMedium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                onClick = {}
            ) {
                Row(
                    modifier = Modifier.padding(Dimensions.PaddingMedium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.InsightsExploreAllPatterns,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun PatternCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.PaddingLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.PaddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(Dimensions.PaddingSmall),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.InsightsPatterns, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {}) {
                        Text("7 Days", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
        Box(modifier = Modifier.padding(padding)) {
            PatternsScreenContent(scrollState = scrollState)
        }
    }
}
