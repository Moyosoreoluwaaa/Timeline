package com.timeline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.cards_around
import timeline.shared.generated.resources.on_fence
import timeline.shared.generated.resources.open_hand
import timeline.shared.generated.resources.secure_lock

internal data class HighlightItem(
    val icon: ImageVector,
    val label: String,
    val range: String,
    val text: String,
)

@Composable
internal fun UsageCard() {
    IntroCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption("Today")
            Caption("3h 42m")
        }
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(Res.drawable.cards_around),
            contentDescription = "Get Started bg",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
internal fun HighlightStack() {
    val items = listOf(
        HighlightItem(
            Icons.Rounded.WbSunny,
            "Morning highlight",
            "6 AM – 12 PM",
            "Two hours of deep research before noon."
        ),
        HighlightItem(
            Icons.Rounded.WbCloudy,
            "Afternoon highlight",
            "12 – 6 PM",
            "A long break around 2, then back to focused work."
        ),
        HighlightItem(
            Icons.Rounded.NightsStay,
            "Evening highlight",
            "6 – 11 PM",
            "You wound down with messages and music after 8."
        ),
    )
    val cardHeight = 124.dp
    val peek = 48.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(cardHeight + peek * (items.size - 1))
        ) {
            items.forEachIndexed { index, item ->
                val depth = items.lastIndex - index
                IntroCard(
                    modifier = Modifier
                        .height(cardHeight)
                        .offset(y = peek * index)
                        .graphicsLayer { scaleX = 1f - depth * 0.06f },
                    padding = 14.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(item.icon)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            item.label,
                            color = introPalette.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Caption(item.range)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.text,
                        color = introPalette.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Image(
            painter = painterResource(Res.drawable.open_hand),
            contentDescription = "Open hand illustration",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
internal fun ExclusionCard() {
    val rows = listOf(
        Icons.Rounded.AccountBalance to "Bank app",
        Icons.Rounded.ChatBubble to "Messages",
        Icons.Rounded.Language to "Chrome",
    )
    val enabled = remember { mutableStateListOf(false, true, true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.secure_lock),
            contentDescription = "Secure lock",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        IntroCard {
            Caption("Screenshots")
            Spacer(Modifier.height(4.dp))
            rows.forEachIndexed { i, (icon, name) ->
                AppRow(icon, name, subtitle = if (enabled[i]) null else "excluded") {
                    Switch(checked = enabled[i], onCheckedChange = { enabled[i] = it })
                }
            }
        }
    }
}

@Composable
internal fun NotificationCard() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(Modifier.fillMaxWidth()) {
            val peek = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            Box(
                Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(peek)
                    .background(introPalette.card.copy(alpha = 0.5f))
            )
            Box(
                Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(peek)
                    .background(introPalette.card.copy(alpha = 0.75f))
            )
            IntroCard(padding = 14.dp) {
                Row {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(introPalette.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            null,
                            tint = introPalette.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Caption("Timeline · now")
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Your afternoon highlight is ready",
                            color = introPalette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "You focused for 2 hours, then wound down around 6.",
                            color = introPalette.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Image(
            painter = painterResource(Res.drawable.on_fence),
            contentDescription = "Character sitting on fence",
            modifier = Modifier
                .offset(x = (-12).dp, y = (-48).dp)
                .size(90.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FocusCard(selected: Set<String>, onToggle: (String) -> Unit) {
    val orangeColor = Color(0xFFFF6F00)
    val whiteColor = Color.White

    IntroCard {
        Caption("Pick a few")
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IntroFocusAreas.forEachIndexed { index, id ->
                val isOn = id in selected
                val isExplanatory = index == 2 || id == "Explanatory"

                val backgroundColor = if (isOn) orangeColor else Color.Transparent
                val textColor = if (isOn) whiteColor else introPalette.textPrimary
                val borderColor = if (isOn) orangeColor else orangeColor.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .then(if (isExplanatory) Modifier.fillMaxWidth() else Modifier)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .border(1.dp, borderColor, CircleShape)
                        .toggleable(
                            value = isOn,
                            role = Role.Checkbox,
                            onValueChange = { onToggle(id) }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
