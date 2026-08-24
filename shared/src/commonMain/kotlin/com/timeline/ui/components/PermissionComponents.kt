package com.timeline.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import kotlinx.coroutines.delay

@Composable
fun PermissionHeader(
    onClose: () -> Unit,
    state: PermissionState
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Everett",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val grantedCount = state.permissions.count { it.isGranted }
                val totalCount = state.permissions.size
                repeat(totalCount) { index ->
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = if (index < grantedCount) Color.Green else Color.Gray
                    ) {}
                    if (index < totalCount - 1) {
                        Box(modifier = Modifier.width(20.dp).height(1.dp).background(Color.Gray))
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionFooter(
    allGranted: Boolean,
    onStartTracking: () -> Unit
) {
    if (allGranted) {
        Button(
            onClick = onStartTracking,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text("Start Activity", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionCard(
    permission: PermissionItem,
    stackIndex: Int,
    totalRemaining: Int,
    onGrant: () -> Unit
) {
    val isFront = stackIndex == 0
    val targetScale = 1f - (stackIndex * 0.05f)
    val targetOffset = -(stackIndex * 20).dp
    val scale by animateFloatAsState(targetValue = targetScale, label = "Scale")
    val offset by animateDpAsState(targetValue = targetOffset, label = "Offset")
    val zIndexValue = (totalRemaining - stackIndex).toFloat()

    var showCheck by remember { mutableStateOf(false) }
    var isCollapsing by remember { mutableStateOf(false) }

    LaunchedEffect(permission.isGranted) {
        if (permission.isGranted) {
            showCheck = true
            delay(500)
            isCollapsing = true
        }
    }

    val collapseProgress by animateFloatAsState(
        targetValue = if (isCollapsing) 0f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    if (collapseProgress > 0f) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .offset(y = offset)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = (1f - collapseProgress) * 500f
                    alpha = collapseProgress
                }
                .zIndex(zIndexValue),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = when(permission.id) {
                            "usage" -> Color(0xFF3498DB).copy(alpha = 0.2f)
                            "overlay" -> Color(0xFFE74C3C).copy(alpha = 0.2f)
                            "notifications" -> Color(0xFFF1C40F).copy(alpha = 0.2f)
                            "accessibility" -> Color(0xFF9B59B6).copy(alpha = 0.2f)
                            "battery" -> Color(0xFF2ECC71).copy(alpha = 0.2f)
                            else -> Color.Green.copy(alpha = 0.2f)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                when(permission.id) {
                                    "usage" -> "\uD83D\uDCC8"
                                    "overlay" -> "\uD83D\uDDA5\ufe0f"
                                    "notifications" -> "\uD83D\uDD14"
                                    "accessibility" -> "\uD83D\uDE4B"
                                    "battery" -> "\uD83D\uDD0B"
                                    else -> "✨"
                                },
                                fontSize = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = permission.title, style = MaterialTheme.typography.titleLarge.copy(color = Color.White), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = permission.description, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.6f)), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onGrant,
                        enabled = isFront && !permission.isVerifying && !permission.isGranted,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        if (permission.isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text("Allow Access", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Not Now", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White), modifier = Modifier.padding(vertical = 8.dp))
                }
                if (showCheck) {
                    Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(80.dp))
                    }
                }
            }
        }
    }
}
