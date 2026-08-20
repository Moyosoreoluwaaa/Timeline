package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeline.domain.Session
import com.timeline.presentation.TimelineEvent

@Composable
fun SessionDetailSheet(
    session: Session,
    isExpanded: Boolean,
    onEvent: (TimelineEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder for App Icon
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(session.packageName, style = MaterialTheme.typography.titleMedium)
                    Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = { onEvent(TimelineEvent.ToggleSheet(!isExpanded)) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Expand")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isExpanded) {
            ExpandedSessionContent(session)
        } else {
            CollapsedSessionContent(session)
        }

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { /* Navigate Prev */ }) {
                Text("Previous")
            }
            TextButton(onClick = { /* Navigate Next */ }) {
                Text("Next")
            }
        }
    }
}

@Composable
fun CollapsedSessionContent(session: Session) {
    // Show up to 3 screenshots in a row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            Surface(
                modifier = Modifier.weight(1f).height(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {}
        }
    }
}

@Composable
fun ExpandedSessionContent(session: Session) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(session.segments) { segment ->
            Column {
                Text(segment.timestamp.toString(), style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    // Placeholder for screenshot
                }
            }
        }
    }
}
