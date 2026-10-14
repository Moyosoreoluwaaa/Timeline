package com.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.RawHighlightEvent
import com.timeline.presentation.RawHighlightViewModel
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Diagnostic Raw Highlight screen for developer inspection of OCR dumps, PII sanitization,
 * entity extractions, and per-app Gemini executive reasoning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawHighlightScreen(
    viewModel: RawHighlightViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToUsageStats: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val animatedItemIds = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            delay(2000)
            showCopiedSnackbar = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            RawHighlightTopBar(
                state = state,
                viewModel = viewModel,
                showDatePicker = showDatePicker,
                onShowDatePickerChange = { showDatePicker = it },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (state.isSyncingDeviceUsage) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }

            if (!state.isUsagePermissionGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(Dimensions.PaddingMedium),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.PaddingMedium)) {
                        Text(text = AppStrings.RealDataPermissionBannerTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = AppStrings.RealDataPermissionBannerDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToUsageStats, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(AppStrings.HighlightGrantUsageAccess, color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }

            if (state.screenshots.isEmpty() && state.selectedScreenshot == null) {
                EmptyHighlightState(
                    onSyncRealData = { viewModel.onEvent(RawHighlightEvent.SyncRealData) },
                    isUsagePermissionGranted = state.isUsagePermissionGranted
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    ScreenshotRail(
                        items = state.uniqueAppItems,
                        selectedPackage = state.selectedPackage,
                        onSelectApp = { viewModel.onEvent(RawHighlightEvent.TogglePackageFilter(it)) }
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    PullToRefreshBox(
                        isRefreshing = state.isSyncingDeviceUsage,
                        onRefresh = { viewModel.onEvent(RawHighlightEvent.SyncRealData) },
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Dimensions.PaddingLarge),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item {
                                AppDailyNarrativeSection(
                                    state = state,
                                    onOptInToggle = { viewModel.onEvent(RawHighlightEvent.ToggleAgentOptIn) },
                                    onGenerate = { viewModel.onEvent(RawHighlightEvent.GenerateReasoning) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(it))
                                        showCopiedSnackbar = true
                                    }
                                )
                            }
                            items(state.filteredScreenshots, key = { it.id }) { item ->
                                SessionHighlightCard(
                                    item = item,
                                    result = state.analysisCache[item.screenshotPath],
                                    localEntities = state.localEntitiesCache[item.screenshotPath],
                                    isAnalyzing = state.analyzingPaths.contains(item.screenshotPath),
                                    shouldAnimate = animatedItemIds[item.id] != true,
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
