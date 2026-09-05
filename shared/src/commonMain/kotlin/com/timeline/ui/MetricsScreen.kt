package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.model.TrialStatus
import com.timeline.presentation.MetricsEvent
import com.timeline.presentation.MetricsViewModel
import com.timeline.ui.components.BarChart
import com.timeline.ui.components.DonutChart
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    onBack: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.InsightsOverview) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = AppStrings.ContentDescBack)
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
        Box(modifier = Modifier.padding(padding)) {
            MetricsScreenContent(
                scrollState = scrollState,
                onNavigateToPaywall = onNavigateToPaywall
            )
        }
    }
}

@Composable
fun MetricsScreenContent(
    scrollState: LazyListState,
    onNavigateToPaywall: () -> Unit = {}
) {
    val viewModel: MetricsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingLarge),
            contentPadding = PaddingValues(top = Dimensions.PaddingMedium, bottom = Dimensions.SpacingMega)
        ) {
            item {
                UsageOverviewHeader(
                    percentage = state.usageChangePercentage,
                    isUp = state.isUsageIncreasing,
                    dailyUsage = state.dailyUsage,
                    period = state.selectedPeriod
                )
            }

            item {
                PatternInsightCard(
                    title = state.biggestPatternTitle,
                    description = state.biggestPatternDescription,
                    percentage = state.biggestPatternPercentage
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    SmallInsightCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.InsightsMostUsedApp,
                        value = state.topAppDisplayName,
                        subValue = state.topAppUsageTime,
                        icon = {
                            AppIcon(
                                icon = state.topAppIcon,
                                contentDescription = state.topAppDisplayName,
                                modifier = Modifier.size(Dimensions.IconMedium)
                            )
                        }
                    )
                    SmallInsightCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.InsightsMostActiveDay,
                        value = state.mostActiveDayName,
                        subValue = state.mostActiveDayUsageTime,
                        icon = {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Dimensions.IconMedium)
                            )
                        }
                    )
                }
            }
        }

        if (state.trialStatus == TrialStatus.EXPIRED || state.trialStatus == TrialStatus.NOT_STARTED) {
            // Locked overlay logic...
        }
    }
}

@Composable
fun UsageOverviewHeader(
    percentage: String,
    isUp: Boolean,
    dailyUsage: List<com.timeline.presentation.DailyUsage>,
    period: com.timeline.presentation.MetricsPeriod
) {
    val vsText = when (period) {
        com.timeline.presentation.MetricsPeriod.DAY -> "vs previous Day"
        com.timeline.presentation.MetricsPeriod.WEEK -> "vs previous 7 days"
        com.timeline.presentation.MetricsPeriod.MONTH -> "vs previous 30 days"
    }
    Column {
        Text(
            text = AppStrings.InsightsOverview.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Dimensions.Half))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = if (isUp) AppStrings.InsightsUsageUp else AppStrings.InsightsUsageDown,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = percentage,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.IconSmall)
                    )
                }
                Text(
                    text = vsText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            BarChart(
                data = dailyUsage,
                modifier = Modifier.width(160.dp).height(100.dp)
            )
        }
    }
}

@Composable
fun PatternInsightCard(
    title: String,
    description: String,
    percentage: Int
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
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Text(text = description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "$percentage% of your usage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            DonutChart(percentage = percentage)
        }
    }
}

@Composable
fun SmallInsightCard(
    title: String,
    value: String,
    subValue: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.PaddingLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
            icon()
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
