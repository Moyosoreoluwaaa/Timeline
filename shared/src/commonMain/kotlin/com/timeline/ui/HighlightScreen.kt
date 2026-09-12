package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.domain.ml.ImageAnalysisResult
import com.timeline.domain.ml.RecognizedLabel
import com.timeline.domain.reasoning.LocalEntities
import com.timeline.presentation.HighlightEvent
import com.timeline.presentation.HighlightScreenshotItem
import com.timeline.presentation.HighlightViewModel
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HighlightScreen(
    viewModel: HighlightViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToUsageStats: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val animatedItemIds = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            kotlinx.coroutines.delay(2000)
            showCopiedSnackbar = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.HighlightTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = AppStrings.ContentDescBack,
                            modifier = Modifier.size(Dimensions.IconSmall)
                        )
                    }
                },
                actions = {
                    val isActive = state.isAiOptedIn
                    val penTint = if (isActive) Color(0xFFFF8C00) else MaterialTheme.colorScheme.onSurfaceVariant
                    IconButton(
                        onClick = { viewModel.onEvent(HighlightEvent.ToggleAiOptIn) },
                        modifier = Modifier.testTag("highlight_pen_button")
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Rounded.Star else Icons.Rounded.Edit,
                            contentDescription = if (isActive) "AI Active (Starburst)" else "Pen (Toggle Reasoning)",
                            tint = penTint,
                            modifier = Modifier.size(Dimensions.IconSmall)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(HighlightEvent.SyncRealData) },
                        modifier = Modifier.testTag("highlight_sync_real_button"),
                        enabled = !state.isSyncingDeviceUsage
                    ) {
                        if (state.isSyncingDeviceUsage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimensions.IconSmall),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = AppStrings.RealDataSyncButton,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Dimensions.IconSmall)
                            )
                        }
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = AppStrings.HighlightPickDate,
                            modifier = Modifier.size(Dimensions.IconSmall)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.selectedDate?.toEpochMilliseconds()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        viewModel.onEvent(HighlightEvent.SelectDate(millis?.let { kotlin.time.Instant.fromEpochMilliseconds(it) }))
                        showDatePicker = false
                    }) {
                        Text(AppStrings.TimelineOk)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(AppStrings.TimelineCancel)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (state.isSyncingDeviceUsage) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!state.isUsagePermissionGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.PaddingMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                        Text(
                            text = AppStrings.RealDataPermissionBannerTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.RealDataPermissionBannerDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToUsageStats,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Grant Usage Access", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }

            if (state.screenshots.isEmpty() && state.selectedScreenshot == null) {
                EmptyHighlightState(
                    onSyncRealData = { viewModel.onEvent(HighlightEvent.SyncRealData) },
                    isUsagePermissionGranted = state.isUsagePermissionGranted
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Unique App Rail (Left, scrollable separately)
                    ScreenshotRail(
                        items = state.uniqueAppItems,
                        selectedPackage = state.selectedPackage,
                        onSelectApp = { viewModel.onEvent(HighlightEvent.TogglePackageFilter(it)) }
                    )

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    PullToRefreshBox(
                        isRefreshing = state.isSyncingDeviceUsage,
                        onRefresh = { viewModel.onEvent(HighlightEvent.SyncRealData) },
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        // Main Content Column (Right, scrollable separately)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Dimensions.PaddingLarge),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item {
                                AppDailyNarrativeSection(
                                    state = state,
                                    onOptInToggle = { viewModel.onEvent(HighlightEvent.ToggleAiOptIn) },
                                    onGenerate = { viewModel.onEvent(HighlightEvent.GenerateReasoning) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(it))
                                        showCopiedSnackbar = true
                                    }
                                )
                            }
                            
                            // Show all filtered screenshots in a timeline
                            items(state.filteredScreenshots, key = { it.id }) { item ->
                                val result = state.analysisCache[item.screenshotPath]
                                val isAnalyzing = state.analyzingPaths.contains(item.screenshotPath)
                                val shouldAnimate = animatedItemIds[item.id] != true
                                
                                SessionHighlightCard(
                                    item = item,
                                    result = result,
                                    localEntities = state.localEntitiesCache[item.screenshotPath],
                                    isAnalyzing = isAnalyzing,
                                    shouldAnimate = shouldAnimate,
                                    onAnimationComplete = { animatedItemIds[item.id] = true },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(it))
                                        showCopiedSnackbar = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Copied Toast
        AnimatedVisibility(
            visible = showCopiedSnackbar,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.padding(bottom = 32.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ) {
                    Text(AppStrings.HighlightCopied, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SessionHighlightCard(
    item: HighlightScreenshotItem,
    result: ImageAnalysisResult?,
    localEntities: LocalEntities?,
    isAnalyzing: Boolean,
    shouldAnimate: Boolean,
    onAnimationComplete: () -> Unit,
    onCopy: (String) -> Unit
) {
    val timeStr = item.timestamp.toString().substring(11, 16)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Time Divider Header (Starts & Ends markers)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$timeStr ${AppStrings.HighlightContentStarts}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (result != null) {
                    // Consolidated Text content with smart typing animation (no app icon, no title, no copy button)
                    TypewriterText(
                        text = result.textResult.fullText,
                        shouldAnimate = shouldAnimate,
                        onAnimationComplete = onAnimationComplete,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Tags Section at Bottom
                    Text(
                        text = AppStrings.HighlightTags,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(result.labels.take(8)) { label ->
                            val confidence = (label.confidence * 100).toInt()
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ) {
                                Text(
                                    text = "${label.text} $confidence%",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Local Entities Section (Emails, Links)
                    if (localEntities != null && (localEntities.emails.isNotEmpty() || localEntities.urls.isNotEmpty())) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            localEntities.emails.forEach { email ->
                                EntityRow(Icons.Rounded.Email, email)
                            }
                            localEntities.urls.forEach { url ->
                                EntityRow(Icons.Rounded.Link, url)
                            }
                        }
                    }
                } else if (isAnalyzing) {
                    // Subtle, compact background processing indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analyzing content in background...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    // Subtle queued indicator while preloading in background
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preloading queued",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${AppStrings.HighlightSessionEnds} at $timeStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ScreenshotRail(
    items: List<HighlightScreenshotItem>,
    selectedPackage: String?,
    onSelectApp: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.width(72.dp).fillMaxHeight(),
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
private fun RailNodeItem(
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
        // Continuous timeline thread
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(Dimensions.LineThickness * 2)
                    .weight(1f)
                    .background(
                        if (isFirst) Color.Transparent
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
            )
            Box(
                modifier = Modifier
                    .width(Dimensions.LineThickness * 2)
                    .weight(1f)
                    .background(
                        if (isLast) Color.Transparent
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
            )
        }

        // Icon Node Container on top of the thread
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            tonalElevation = if (isSelected) Dimensions.CardElevation else Dimensions.None
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
private fun TypewriterText(
    text: String,
    shouldAnimate: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (!shouldAnimate) {
        // Direct instant rendering for already-seen items or existing contents
        Text(
            text = text,
            style = style,
            fontFamily = fontFamily,
            color = color,
            modifier = modifier
        )
        return
    }

    var displayedLength by remember(text) { mutableIntStateOf(0) }
    var isAnimationComplete by remember(text) { mutableStateOf(false) }

    LaunchedEffect(text) {
        displayedLength = 0
        isAnimationComplete = false
        if (text.isEmpty()) {
            isAnimationComplete = true
            onAnimationComplete()
            return@LaunchedEffect
        }
        val totalChars = text.length
        val step = when {
            totalChars > 600 -> 6
            totalChars > 300 -> 4
            totalChars > 150 -> 2
            else -> 1
        }
        val delayMs = 10L

        var current = 0
        while (current < totalChars) {
            current = (current + step).coerceAtMost(totalChars)
            displayedLength = current
            kotlinx.coroutines.delay(delayMs)
        }
        isAnimationComplete = true
        onAnimationComplete()
    }

    val visibleText = remember(displayedLength, text) {
        text.take(displayedLength)
    }

    Text(
        text = if (!isAnimationComplete) "$visibleText ▌" else visibleText,
        style = style,
        fontFamily = fontFamily,
        color = color,
        modifier = modifier.clickable {
            displayedLength = text.length
            isAnimationComplete = true
            onAnimationComplete()
        }
    )
}


@Composable
private fun EntityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AppDailyNarrativeSection(
    state: com.timeline.presentation.HighlightState,
    onOptInToggle: () -> Unit,
    onGenerate: () -> Unit,
    onCopy: (String) -> Unit
) {
    if (!state.isAiOptedIn) {
        AiOptInBanner(onOptIn = onOptInToggle)
        return
    }

    if (state.isReasoningLoading) {
        val stageText = when (state.reasoningStage) {
            com.timeline.presentation.HighlightState.ReasoningStage.REDACTING -> "Sanitizing PII data (Privacy Shield active)..."
            com.timeline.presentation.HighlightState.ReasoningStage.DEDUPLICATING -> "Deduplicating session captures..."
            com.timeline.presentation.HighlightState.ReasoningStage.CONTEXTUALIZING -> "Injecting historical & contextual timeline..."
            com.timeline.presentation.HighlightState.ReasoningStage.REASONING -> "Synthesizing Gemini executive narrative..."
            else -> "Analyzing usage activity & generating executive narratives..."
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini Reasoning Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stageText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.reasoningProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        return
    }

    val selectedPackage = state.selectedPackage
    val reasoning = if (selectedPackage != null) {
        state.appDailyReasonings[selectedPackage]
    } else {
        state.appDailyReasonings["ALL_APPS"] ?: state.appDailyReasonings.values.firstOrNull()
    }

    val narrativeTitle = if (selectedPackage != null) {
        val appName = state.filteredScreenshots.firstOrNull()?.displayName ?: selectedPackage
        "AI Narrative Insight • $appName"
    } else {
        "Daily Executive Narrative • All Apps"
    }

    if (reasoning != null) {
        var showPrivacyInfo by remember { mutableStateOf(false) }

        if (showPrivacyInfo) {
            AlertDialog(
                onDismissRequest = { showPrivacyInfo = false },
                title = { Text("Privacy Shield Active") },
                text = { Text("PII Redactor successfully detected and scrubbed sensitive data (emails, phone numbers, auth tokens, cards) locally on-device before Gemini reasoning.") },
                confirmButton = {
                    TextButton(onClick = { showPrivacyInfo = false }) {
                        Text("Got it")
                    }
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = narrativeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    if (state.isPrivacyShieldActive) {
                        Badge(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { showPrivacyInfo = true },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Shield, contentDescription = "Privacy Shield", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sanitized",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    val modeIcon = when (state.categoryIcon) {
                        "brain" -> Icons.Rounded.Psychology
                        "chat" -> Icons.Rounded.Forum
                        "people" -> Icons.Rounded.People
                        "terminal" -> Icons.Rounded.Terminal
                        "play" -> Icons.Rounded.PlayArrow
                        else -> Icons.Rounded.Info
                    }

                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(modeIcon, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reasoning.cognitiveMode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TypewriterText(
                    text = reasoning.summary,
                    shouldAnimate = true,
                    onAnimationComplete = {},
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (reasoning.actionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detected Tasks & Next Steps",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                val taskBlock = reasoning.actionItems.joinToString("\n") { "• $it" }
                                onCopy(taskBlock)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy Tasks",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    reasoning.actionItems.forEach { item ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    } else {
        // Option to generate if not yet generated
        OutlinedButton(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (selectedPackage != null) "Generate AI Narrative for this app" else "Synthesize Daily Gemini Narrative")
        }
    }
}

@Composable
private fun AiOptInBanner(onOptIn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Security, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI Activity Reasoning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Turn your raw screen captures into meaningful narratives. Data is processed via Gemini Cloud for reasoning. No data is used for model training.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOptIn,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enable AI Narratives")
            }
        }
    }
}

@Composable
private fun EmptyHighlightState(
    onSyncRealData: () -> Unit,
    isUsagePermissionGranted: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.SpacingGiant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.ImageSearch,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

        Text(
            text = AppStrings.HighlightNoHighlights,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))

        Text(
            text = if (isUsagePermissionGranted)
                "No activity captured yet. Use your apps or tap Sync to load your recent timeline activity."
            else
                AppStrings.RealDataPermissionBannerDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium)
        )

        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))

        Button(
            onClick = onSyncRealData,
            modifier = Modifier.testTag("empty_state_sync_real_button")
        ) {
            Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(AppStrings.RealDataSyncButton)
        }
    }
}
