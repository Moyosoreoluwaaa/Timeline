package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    
    var selectedTab by remember { mutableStateOf(0) }
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
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> MetricsScreenContent(scrollState = insightsListState)
                1 -> AppsScreenContent(scrollState = appsListState)
                2 -> TrendsScreenContent(scrollState = trendsListState)
                3 -> PatternsScreenContent(scrollState = patternsListState)
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
                TabItem(AppStrings.InsightsTrends, Icons.Rounded.TrendingUp),
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

data class TabItem(val name: String, val icon: ImageVector)

@Composable
fun ToolbarTab(
    item: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.SpacingExtraLarge))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = Dimensions.ToolbarTabPadding, vertical = Dimensions.PaddingSmall),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = contentColor,
                modifier = Modifier.size(Dimensions.IconSmall)
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(start = Dimensions.Half)
                )
            }
        }
    }
}
