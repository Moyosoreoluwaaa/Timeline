package com.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timeline.presentation.RawHighlightScreenshotItem
import com.timeline.ui.theme.Dimensions

@Composable
internal fun ScreenshotRail(
    items: List<RawHighlightScreenshotItem>,
    selectedPackage: String?,
    onSelectApp: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items, key = { _, item -> item.packageName }) { index, item ->
            val isSelected = item.packageName == selectedPackage
            val isFirst = index == 0
            val isLast = index == items.lastIndex
            RailNodeItem(
                isSelected = isSelected,
                isFirst = isFirst,
                isLast = isLast,
                onClick = { onSelectApp(item.packageName) }
            ) {
                AppIcon(
                    icon = item.icon,
                    contentDescription = item.displayName,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
internal fun RailNodeItem(
    isSelected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(Dimensions.LineThickness * 2)
                    .weight(1f)
                    .background(
                        if (isFirst) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.6f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .width(Dimensions.LineThickness * 2)
                    .weight(1f)
                    .background(
                        if (isLast) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.6f
                        )
                    )
            )
        }

        Surface(
            modifier = Modifier
                .size(48.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = 0.5f
                )
            ),
            tonalElevation = if (isSelected) Dimensions.CardElevation else Dimensions.None
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
