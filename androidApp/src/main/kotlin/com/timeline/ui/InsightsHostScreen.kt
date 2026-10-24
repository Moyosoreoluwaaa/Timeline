package com.timeline.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.MetricsEvent
import com.timeline.presentation.MetricsPeriod
import com.timeline.presentation.MetricsViewModel
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsHostScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: MetricsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPeriodMenu by remember { mutableStateOf(false) }
    
    val insightsListState = rememberLazyListState()
    val appsListState = rememberLazyListState()
    val trendsListState = rememberLazyListState()
    val patternsListState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.TimelineTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = AppStrings.ContentDescBack)
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { showPeriodMenu = true }) {
                            val periodText = when (state.selectedPeriod) {
                                MetricsPeriod.DAY -> "Day"
                                MetricsPeriod.WEEK -> "7 Days"
                                MetricsPeriod.MONTH -> "30 Days"
                            }
                            Text(periodText, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = showPeriodMenu,
                            onDismissRequest = { showPeriodMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Day") },
                                onClick = {
                                    viewModel.onEvent(MetricsEvent.ChangePeriod(MetricsPeriod.DAY))
                                    showPeriodMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("7 Days") },
                                onClick = {
                                    viewModel.onEvent(MetricsEvent.ChangePeriod(MetricsPeriod.WEEK))
                                    showPeriodMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("30 Days") },
                                onClick = {
                                    viewModel.onEvent(MetricsEvent.ChangePeriod(MetricsPeriod.MONTH))
                                    showPeriodMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = Dimensions.SpacingLarge, topEnd = Dimensions.SpacingLarge)),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> MetricsScreenContent(scrollState = insightsListState)
                        1 -> AppsScreenContent(scrollState = appsListState)
                        2 -> TrendsScreenContent(scrollState = trendsListState)
                        3 -> PatternsScreenContent(scrollState = patternsListState)
                    }
                }
            }

            FloatingToolbar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Dimensions.SpacingLarge)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
fun FloatingToolbar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = Dimensions.PaddingLarge)
            .animateContentSize(),
        shape = RoundedCornerShape(Dimensions.SpacingExtraLarge),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
        shadowElevation = Dimensions.ModalElevation,
        tonalElevation = Dimensions.ModalElevation
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.PaddingSmall)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)
        ) {
            val tabs = listOf(
                TabItem(AppStrings.InsightsOverview, Icons.Rounded.AutoFixHigh),
                TabItem(AppStrings.InsightsApps, Icons.Rounded.GridView),
                TabItem(AppStrings.InsightsTrends, Icons.AutoMirrored.Rounded.TrendingUp),
                TabItem(AppStrings.InsightsPatterns, Icons.Rounded.AutoAwesome)
            )

            tabs.forEachIndexed { index, tab ->
                ToolbarTab(
                    item = tab,
                    isSelected = selectedTab == index,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}
