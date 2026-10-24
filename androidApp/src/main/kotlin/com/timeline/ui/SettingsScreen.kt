package com.timeline.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.SettingsEvent
import com.timeline.presentation.SettingsViewModel
import com.timeline.tutorial.TutorialStep
import com.timeline.tutorial.TutorialViewModel
import com.timeline.ui.components.SettingsTopBar
import com.timeline.ui.components.TopAppBarCutoutRadius
import com.timeline.util.rememberEmailLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    tutorialViewModel: TutorialViewModel = org.koin.compose.viewmodel.koinViewModel(),
    onNavigateToPaywall: (isDeals: Boolean) -> Unit = {},
    onNavigateToCustomerCenter: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tutorialState by tutorialViewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
    var showReasoningSheet by remember { mutableStateOf(false) }
    var showExclusionsSheet by remember { mutableStateOf(false) }
    var showRetentionSheet by remember { mutableStateOf(false) }
    var showBugReportSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }

    val launchEmail = rememberEmailLauncher()

    LaunchedEffect(tutorialState.currentStep) {
        when (tutorialState.currentStep) {
            TutorialStep.SPOTLIGHT_HIGHLIGHTS_PREFERENCE -> {
                showReasoningSheet = false
                scrollState.animateScrollToItem(0)
            }

            TutorialStep.SPOTLIGHT_REASONING_SHEET -> {
                showReasoningSheet = true
            }

            TutorialStep.SPOTLIGHT_TRACKING_OPTIONS -> {
                showReasoningSheet = false
                scrollState.animateScrollToItem(3)
            }

            TutorialStep.SPOTLIGHT_APP_EXCLUSIONS -> {
                showExclusionsSheet = false
                scrollState.animateScrollToItem(8)
            }

            TutorialStep.SPOTLIGHT_EXCLUSIONS_SHEET -> {
                // Spotlight App Exclusion card was tapped — open the sheet
                showExclusionsSheet = true
            }

            TutorialStep.SPOTLIGHT_DATA_RETENTION -> {
                // Exclusions sheet step done — close it and spotlight Data Retention
                showExclusionsSheet = false
                showRetentionSheet = false
                scrollState.animateScrollToItem(9)
            }

            TutorialStep.SPOTLIGHT_RETENTION_SHEET -> {
                // Data Retention card was tapped — open the retention sheet
                showRetentionSheet = true
            }

            else -> {
                if (tutorialState.isActive) {
                    showReasoningSheet = false
                    showExclusionsSheet = false
                    showRetentionSheet = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.onEvent(SettingsEvent.LoadSettings) }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.statusBars,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            SettingsTopBar(
                isLoggedIn = state.isLoggedIn,
                isPro = state.isPro,
                onBack = onBack,
                onNavigateToAuth = onNavigateToAuth,
                onLogout = { viewModel.onEvent(SettingsEvent.Logout) },
                onOpenAbout = { showAboutSheet = true },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val topPadding = (padding.calculateTopPadding() - TopAppBarCutoutRadius).coerceAtLeast(0.dp)
        com.timeline.ui.settings.SettingsListContent(
            state = state,
            viewModel = viewModel,
            tutorialState = tutorialState,
            tutorialViewModel = tutorialViewModel,
            scrollState = scrollState,
            topPadding = topPadding,
            navBarPadding = navBarPadding,
            onNavigateToPaywall = onNavigateToPaywall,
            onNavigateToCustomerCenter = onNavigateToCustomerCenter,
            onOpenReasoningSheet = { showReasoningSheet = true },
            onOpenExclusionsSheet = { showExclusionsSheet = true },
            onOpenRetentionSheet = { showRetentionSheet = true },
            onOpenBugReportSheet = { showBugReportSheet = true },
            onContactUs = { launchEmail("moyokamaal@gmail.com", "Timeline App Support", "") }
        )


        // --- Bottom Sheets ---

        if (showAboutSheet) {
            com.timeline.ui.settings.AboutSheet(
                version = state.appVersion,
                onDismiss = { showAboutSheet = false },
                onContactSupport = { launchEmail("moyokamaal@gmail.com", "Timeline App Support", "") }
            )
        }

        if (showBugReportSheet) {
            com.timeline.ui.settings.BugReportSheet(
                onDismiss = { showBugReportSheet = false },
                onSubmit = { desc ->
                    launchEmail(
                        "moyokamaal@gmail.com",
                        "Timeline App Bug Report",
                        "Bug Details:\n$desc"
                    )
                }
            )
        }

        if (showReasoningSheet) {
            com.timeline.ui.settings.ReasoningModeSheet(
                selectedMode = state.highlightReasoningMode,
                digestFrequency = state.digestFrequency,
                isPro = state.isPro,
                isTrialActive = state.trialStatus == com.timeline.domain.model.TrialStatus.ACTIVE,
                tutorialStep = tutorialState.currentStep,
                tutorialViewModel = tutorialViewModel,
                onDismiss = { showReasoningSheet = false },
                onSelectMode = { mode -> viewModel.onEvent(SettingsEvent.SetReasoningMode(mode)) },
                onFrequencyChange = { freq -> viewModel.onEvent(SettingsEvent.SetDigestFrequency(freq)) },
                onNavigateToPaywall = { onNavigateToPaywall(false) }
            )
        }

        if (showExclusionsSheet) {
            com.timeline.ui.settings.AppExclusionsSheet(
                state = state,
                isTutorialActive = tutorialState.isActive,
                tutorialStep = tutorialState.currentStep,
                tutorialViewModel = tutorialViewModel,
                onDismiss = { showExclusionsSheet = false },
                onToggleExclusion = { pkg -> viewModel.onEvent(SettingsEvent.ToggleExclusion(pkg)) },
                onNavigateToPaywall = onNavigateToPaywall
            )
        }

        if (showRetentionSheet) {
            com.timeline.ui.settings.DataRetentionSheet(
                state = state,
                tutorialStep = tutorialState.currentStep,
                tutorialViewModel = tutorialViewModel,
                onDismiss = { showRetentionSheet = false },
                onRetentionChange = { days -> viewModel.onEvent(SettingsEvent.SetDataRetention(days)) },
                onNavigateToPaywall = { onNavigateToPaywall(false) }
            )
        }
    }
}
