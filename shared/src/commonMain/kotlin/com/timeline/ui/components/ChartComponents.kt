package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timeline.presentation.DailyUsage
import com.timeline.presentation.StackedUsage
import com.timeline.presentation.UsageTrend
import com.timeline.ui.theme.Dimensions

@Composable
fun BarChart(
    data: List<DailyUsage>,
    modifier: Modifier = Modifier
) {
    val maxUsage = data.maxOfOrNull { it.totalMinutes } ?: 1L
    
    Row(
        modifier = modifier.fillMaxWidth().height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { usage ->
            val barHeight = (usage.totalMinutes.toFloat() / maxUsage.toFloat()).coerceIn(0.1f, 1f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(barHeight)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(Dimensions.Half))
                Text(
                    text = usage.day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StackedBarChart(
    data: List<StackedUsage>,
    modifier: Modifier = Modifier
) {
    val maxUsage = data.maxOfOrNull { it.appDistributions.sumOf { d -> d.minutes } } ?: 1L

    Row(
        modifier = modifier.fillMaxWidth().height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { usage ->
            val totalMinutes = usage.appDistributions.sumOf { it.minutes }
            val barHeight = (totalMinutes.toFloat() / maxUsage.toFloat()).coerceIn(0.1f, 1f)
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(barHeight)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        usage.appDistributions.forEach { dist ->
                            val weight = dist.minutes.toFloat() / totalMinutes.toFloat()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(weight.coerceAtLeast(0.01f))
                                    .background(Color(dist.color))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.Half))
                Text(
                    text = usage.day,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun LineChartPlaceholder(
    data: List<UsageTrend>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val maxVal = data.maxOfOrNull { it.minutes } ?: 1L
        data.forEach { trend ->
            val height = (trend.minutes.toFloat() / maxVal.toFloat()).coerceIn(0.1f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(height)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
                Text(trend.timeLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
