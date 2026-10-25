package com.timeline.ui.reel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.domain.model.ReelSlide
import com.timeline.ui.theme.AppColors

/**
 * Shareable "concert ticket" snapshot of a highlight slide.
 * Slides up from the bottom of the reel as an overlay sheet.
 */
@Composable
fun ReelHighlightTicketView(
    slide: ReelSlide,
    dateLabel: String,
    periodTitle: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShared by remember { mutableStateOf(false) }

    val ticketBg = Color(slide.bgColorHex)
    val ticketFg = Color(slide.fgColorHex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF18181B))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF5A5A60))
        )

        // Perforated Ticket Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(ticketBg)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timeline",
                        color = ticketFg,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateLabel,
                        color = ticketFg.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Headline
                Text(
                    text = slide.headline,
                    color = ticketFg,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                )

                // Optional Stat
                if (slide.stat != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(slide.accentColorHex).copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = slide.stat,
                            color = ticketFg,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = slide.sub ?: "",
                            color = ticketFg.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Dashed separator
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                ) {
                    drawLine(
                        color = ticketFg.copy(alpha = 0.35f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                }

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your day, remembered",
                            color = ticketFg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$periodTitle recap",
                            color = ticketFg.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    // Simulated barcode
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(24.dp)
                    ) {
                        listOf(2, 4, 1, 3, 2, 4, 2, 1, 3, 4, 2).forEach { w ->
                            Box(
                                modifier = Modifier
                                    .width(w.dp)
                                    .height(24.dp)
                                    .background(ticketFg.copy(alpha = 0.85f))
                            )
                        }
                    }
                }
            }
        }

        // Share button
        Button(
            onClick = { isShared = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.BrandOrange,
                contentColor = AppColors.OnOrangeDark
            ),
            shape = RoundedCornerShape(99.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(48.dp)
        ) {
            Icon(
                imageVector = if (isShared) Icons.Rounded.Check else Icons.Rounded.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isShared) "Ticket Ready to Share" else "Share Highlight Ticket",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        TextButton(
            onClick = onClose,
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = "Close",
                color = Color(0xFFA8A8B0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
