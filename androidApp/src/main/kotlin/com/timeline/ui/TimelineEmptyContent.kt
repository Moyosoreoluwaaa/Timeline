package com.timeline.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.timeline.R
import com.timeline.domain.Session
import com.timeline.ui.components.TimelineEntry
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlin.time.Clock

@Composable
internal fun TimelineEmptyContent(
    isShowingShimmer: Boolean,
    navBarPadding: Dp,
    modifier: Modifier = Modifier
) {
    if (isShowingShimmer) {
        val alpha by rememberInfiniteTransition(label = "ShimmerTransition").animateFloat(
            initialValue = 0.3f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ShimmerAlpha"
        )
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            userScrollEnabled = false,
            contentPadding = PaddingValues(
                top = Dimensions.PaddingSmall,
                bottom = navBarPadding + (Dimensions.PaddingLarge * 2)
            )
        ) {
            items(5) { index ->
                Box(modifier = Modifier.alpha(alpha)) {
                    TimelineEntry(
                        session = Session(
                            id = "shimmer_$index",
                            packageName = "",
                            displayName = "",
                            startTime = Clock.System.now(),
                            endTime = Clock.System.now(),
                            durationMinutes = 10
                        ),
                        isFirst = index == 0,
                        isLast = index == 4,
                        onClick = {}
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.empty_state_illustration),
                contentDescription = "Empty activity state",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = Dimensions.PaddingSmall)
            )

            val formattedEmptyText = AppStrings.TimelineNoActivity.let { text ->
                val words = text.split(" ")
                if (words.size >= 7 && words[6].endsWith(",")) {
                    words.take(7).joinToString(" ") + "\n" + words.drop(4).joinToString(" ")
                } else {
                    text.replace(", ", ",\n")
                }
            }

            Text(
                text = formattedEmptyText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
