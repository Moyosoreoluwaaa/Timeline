package com.timeline.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
        modifier = modifier.fillMaxWidth().height(Dimensions.ChartHeightMedium),
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
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (usage.isWeekend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(Dimensions.Half))
                Text(
                    text = usage.day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    percentage: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 20f
) {
    Box(
        modifier = modifier.size(Dimensions.DonutChartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = color.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (percentage.toFloat() / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
fun LineChartPlaceholder(
    data: List<UsageTrend>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxWidth().height(Dimensions.ChartHeightMedium)) {
        if (data.isEmpty()) return@Canvas
        
        val maxVal = data.maxOfOrNull { it.minutes } ?: 1L
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)
        
        val path = Path()
        val fillPath = Path()
        
        data.forEachIndexed { index, trend ->
            val x = index * stepX
            val y = height - (trend.minutes.toFloat() / maxVal.toFloat() * height)
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (index == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
            )
        )
        
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        
        // Draw points
        data.forEachIndexed { index, trend ->
            val x = index * stepX
            val y = height - (trend.minutes.toFloat() / maxVal.toFloat() * height)
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(x, y),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
fun StackedBarChart(
    data: List<StackedUsage>,
    modifier: Modifier = Modifier
) {
    // Keep for backward compatibility or future use, not currently in redesign mockup
}
