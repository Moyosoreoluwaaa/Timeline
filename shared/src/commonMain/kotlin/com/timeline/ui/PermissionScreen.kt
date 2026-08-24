package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.timeline.presentation.PermissionViewModel
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionEffect
import com.timeline.presentation.PermissionItem
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBatteryOptimization: () -> Unit,
    onAllGranted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PermissionEffect.NavigateToUsageStatsSettings -> onNavigateToUsageStats()
                is PermissionEffect.NavigateToOverlaySettings -> onNavigateToOverlay()
                is PermissionEffect.RequestNotificationPermission -> onNavigateToNotification()
                is PermissionEffect.NavigateToAccessibilitySettings -> onNavigateToAccessibility()
                is PermissionEffect.NavigateToBatteryOptimizationSettings -> onNavigateToBatteryOptimization()
                is PermissionEffect.AllGranted -> onAllGranted()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(PermissionEvent.CheckPermissions)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE67E22),
                        Color(0xFFF1C40F),
                        Color(0xFF000000)
                    )
                )
            )
    ) {
        IconButton(
            onClick = { /* Handle close */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
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

            // Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val grantedCount = state.permissions.count { it.isGranted }
                val totalCount = state.permissions.size
                
                repeat(totalCount) { index ->
                    val isGranted = index < grantedCount
                    Surface(
                        modifier = Modifier.size(8.dp), 
                        shape = CircleShape, 
                        color = if (isGranted) Color.Green else Color.Gray
                    ) {}
                    if (index < totalCount - 1) {
                        Box(modifier = Modifier.width(20.dp).height(1.dp).background(Color.Gray))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.allGranted) "You're all set!" else "Almost there",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = if (state.allGranted) "Everything is ready for your timeline." else "To show you real insights, we need\naccess to how you use your apps.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (state.allGranted) {
                    Button(
                        onClick = { viewModel.onEvent(PermissionEvent.StartTracking) },
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
                } else {
                    val remainingPermissions = state.permissions.filter { !it.isGranted }
                    
                    remainingPermissions.asReversed().forEachIndexed { index, permission ->
                        val stackIndex = remainingPermissions.size - 1 - index
                        PermissionCard(
                            permission = permission,
                            stackIndex = stackIndex,
                            totalRemaining = remainingPermissions.size,
                            onGrant = { viewModel.onEvent(PermissionEvent.GrantPermission(permission.id)) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
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
    // stackIndex 0 is the front card
    val isFront = stackIndex == 0
    
    val targetScale = 1f - (stackIndex * 0.05f)
    val targetOffset = -(stackIndex * 20).dp
    
    val scale by animateFloatAsState(targetValue = targetScale, label = "Scale")
    val offset by animateDpAsState(targetValue = targetOffset, label = "Offset")
    val zIndex = (totalRemaining - stackIndex).toFloat()

    var showCheck by remember { mutableStateOf(false) }
    var isCollapsing by remember { mutableStateOf(false) }

    LaunchedEffect(permission.isVerifying) {
        if (permission.isVerifying) {
            // Already handled by delay in VM, but we can sync local state if needed
        }
    }

    // When the card gets granted, it should animate the check then collapse
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
                    translationY = (1f - collapseProgress) * 500f // Move down as it collapses
                    alpha = collapseProgress
                }
                .zIndex(zIndex),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C1C1E)
            )
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

                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = permission.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.6f)),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onGrant,
                        enabled = isFront && !permission.isVerifying && !permission.isGranted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        if (permission.isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text("Allow Access", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (showCheck) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        }
    }
}
