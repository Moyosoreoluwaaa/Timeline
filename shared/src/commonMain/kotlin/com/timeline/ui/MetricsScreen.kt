package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.MetricsEvent
import com.timeline.presentation.MetricsViewModel
import com.timeline.ui.components.BarChart
import com.timeline.ui.components.LineChartPlaceholder
import com.timeline.ui.components.StackedBarChart
import com.timeline.ui.theme.Dimensions
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    viewModel: MetricsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights & Metrics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingLarge),
                contentPadding = PaddingValues(vertical = Dimensions.PaddingMedium)
            ) {
                item {
                    TrialBanner(
                        status = state.trialStatus,
                        timeRemaining = state.trialTimeRemaining
                    )
                }

                item {
                    MetricSection(title = "Daily Usage Activity") {
                        BarChart(data = state.dailyUsage)
                    }
                }

                item {
                    MetricSection(title = "App Distribution (Stacked)") {
                        StackedBarChart(data = state.appUsageStacked)
                    }
                }

                item {
                    MetricSection(title = "Temporal Trends") {
                        LineChartPlaceholder(data = state.usageTrends)
                    }
                }
            }

            if (state.trialStatus == TrialStatus.EXPIRED || state.trialStatus == TrialStatus.NOT_STARTED) {
                LockedOverlay(
                    status = state.trialStatus,
                    onUpgradeClick = onNavigateToPaywall,
                    onStartTrial = { viewModel.onEvent(MetricsEvent.StartTrial) }
                )
            }
        }
    }
}

@Composable
fun MetricSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            content()
        }
    }
}

@Composable
fun TrialBanner(
    status: TrialStatus,
    timeRemaining: String
) {
    if (status == TrialStatus.ACTIVE) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(Dimensions.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Trial Active (Simulation)", style = MaterialTheme.typography.labelLarge)
                Text("Ends in: $timeRemaining", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LockedOverlay(
    status: TrialStatus,
    onUpgradeClick: () -> Unit,
    onStartTrial: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(Dimensions.PaddingLarge),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.PaddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp))
                Text(
                    text = if (status == TrialStatus.EXPIRED) "Trial Expired" else "Unlock Insights",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Unlock advanced charts, app distribution, and temporal trends with Timeline Pro.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = if (status == TrialStatus.NOT_STARTED) onStartTrial else onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (status == TrialStatus.NOT_STARTED) "Start 15-Min Trial" else "Upgrade to Pro")
                }
            }
        }
    }
}
