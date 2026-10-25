package com.timeline.ui.reel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.TimeOfDayFilter
import com.timeline.ui.theme.AppColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun ReelDayTimelineBar(
    currentFilter: TimeOfDayFilter,
    onFilterSelected: (TimeOfDayFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val morningColor = AppColors.WindowMorningAmber
    val afternoonColor = AppColors.WindowAfternoonTeal
    val eveningColor = AppColors.WindowEveningIndigo

    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz)
    val currentHourDecimal = now.hour + (now.minute / 60f) // e.g. 14.5 for 2:30 PM

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
        ) {
            val totalWidth = maxWidth
            val gap = 4.dp
            val availableWidth = (totalWidth - (gap * 2)).coerceAtLeast(0.dp)

            // Day scale: 6 AM to 10 PM = 16 hours
            // Morning: 6 AM - 12 PM (6h) = 6/16
            // Afternoon: 12 PM - 5 PM (5h) = 5/16
            // Evening: 5 PM - 10 PM (5h) = 5/16
            val clampedHour = currentHourDecimal.coerceIn(6f, 22f)
            val progressFraction = (clampedHour - 6f) / 16f
            val nowX = availableWidth * progressFraction + (if (clampedHour >= 12f) gap else 0.dp) + (if (clampedHour >= 17f) gap else 0.dp)

            // Segmented colored pill bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                // Morning (6 AM - 12 PM = 6 hrs)
                val morningActive = currentFilter == TimeOfDayFilter.ALL || currentFilter == TimeOfDayFilter.MORNING
                Box(
                    modifier = Modifier
                        .weight(6f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (morningActive) morningColor else morningColor.copy(alpha = 0.35f))
                        .clickable { onFilterSelected(TimeOfDayFilter.MORNING) }
                )

                // Afternoon (12 PM - 5 PM = 5 hrs)
                val afternoonActive = currentFilter == TimeOfDayFilter.ALL || currentFilter == TimeOfDayFilter.AFTERNOON
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (afternoonActive) afternoonColor else afternoonColor.copy(alpha = 0.35f))
                        .clickable { onFilterSelected(TimeOfDayFilter.AFTERNOON) }
                )

                // Evening (5 PM - 10 PM = 5 hrs)
                val eveningActive = currentFilter == TimeOfDayFilter.ALL || currentFilter == TimeOfDayFilter.EVENING
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (eveningActive) eveningColor else eveningColor.copy(alpha = 0.35f))
                        .clickable { onFilterSelected(TimeOfDayFilter.EVENING) }
                )
            }

            // "Now" pin indicator marking the current time
            if (currentHourDecimal in 6f..22f) {
                val pinPillWidth = 38.dp
                val pinOffset = (nowX - (pinPillWidth / 2)).coerceIn(0.dp, totalWidth - pinPillWidth)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(x = pinOffset, y = (-22).dp)
                        .width(pinPillWidth)
                ) {
                    // Pin head badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.onSurface)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Now",
                            color = MaterialTheme.colorScheme.surface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 12.sp
                        )
                    }
                    // Pin needle penetrating through the bar
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }

        // Peripheral time tick labels (6 AM, 12, 5 PM, 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "6 AM",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "12 PM",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "5 PM",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "10 PM",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
