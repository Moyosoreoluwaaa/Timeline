package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.AppInfoProvider
import com.timeline.domain.DeviceUsageSyncer
import com.timeline.domain.SubscriptionManager
import com.timeline.domain.UserPreferences
import com.timeline.domain.ml.HighlightAnalysisCache
import com.timeline.domain.ml.VisionAnalysisService
import com.timeline.domain.reasoning.HighlightReasoningMode
import com.timeline.domain.reasoning.LocalHeuristicService
import com.timeline.domain.reasoning.ReasoningService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Diagnostic ViewModel managing raw screenshot OCR extractions, PII redactions, and app-level reasonings.
 */
class RawHighlightViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: AppInfoProvider,
    private val visionService: VisionAnalysisService,
    private val reasoningService: ReasoningService,
    private val localHeuristicService: LocalHeuristicService,
    private val userPreferences: UserPreferences,
    private val subscriptionManager: SubscriptionManager,
    private val deviceUsageSyncer: DeviceUsageSyncer,
    private val logger: Logger,
) : ViewModel() {
    private val analysisSemaphore = Semaphore(2)
    private val _state = MutableStateFlow(
        RawHighlightState(
            analysisCache = HighlightAnalysisCache.getAll(),
            isUsagePermissionGranted = deviceUsageSyncer.hasPermission()
        )
    )
    val state: StateFlow<RawHighlightState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<RawHighlightEffect>()
    val effects: SharedFlow<RawHighlightEffect> = _effects.asSharedFlow()

    private var timelineJob: Job? = null
    private val coordinator = RawHighlightReasoningCoordinator(
        repository, appInfoProvider, visionService, reasoningService, localHeuristicService, logger
    )

    init {
        loadScreenshots()
        viewModelScope.launch {
            combine(subscriptionManager.isPro, userPreferences.state) { isPro, prefs ->
                _state.update { it.copy(
                    isPro = isPro,
                    isAgentOptedIn = prefs.isAgentReasoningEnabled,
                    currentReasoningMode = prefs.highlightReasoningMode
                ) }
            }.collect {}
        }
    }

    fun onEvent(event: RawHighlightEvent) {
        when (event) {
            is RawHighlightEvent.Refresh, is RawHighlightEvent.LoadScreenshots -> loadScreenshots()
            is RawHighlightEvent.SelectScreenshot -> selectScreenshot(event.item)
            is RawHighlightEvent.AnalyzeScreenshot -> analyzeScreenshot(event.item)
            is RawHighlightEvent.UpdateSearchQuery -> _state.update { it.copy(searchQuery = event.query) }
            is RawHighlightEvent.ClearSearch -> _state.update { it.copy(searchQuery = "") }
            is RawHighlightEvent.TogglePackageFilter -> _state.update {
                it.copy(selectedPackage = if (it.selectedPackage == event.packageName) null else event.packageName)
            }
            is RawHighlightEvent.SelectDate -> {
                _state.update { it.copy(selectedDate = event.date) }
                loadScreenshots()
            }
            is RawHighlightEvent.ToggleAgentOptIn -> viewModelScope.launch {
                userPreferences.setAgentReasoningEnabled(!_state.value.isAgentOptedIn)
            }
            is RawHighlightEvent.GenerateReasoning -> generateDailyReasoning()
            is RawHighlightEvent.SetReasoningMode -> viewModelScope.launch {
                userPreferences.setHighlightReasoningMode(event.mode)
                _state.update { it.copy(currentReasoningMode = event.mode) }
                generateDailyReasoning()
            }
            is RawHighlightEvent.SyncRealData -> syncRealData()
        }
    }

    private fun loadScreenshots() {
        timelineJob?.cancel()
        timelineJob = viewModelScope.launch {
            repository.getTimeline().collect { sessions ->
                val tz = TimeZone.currentSystemDefault()
                val (effectiveDate, effectiveLocalDate) = RawHighlightScreenshotMapper.findEffectiveDate(
                    sessions, _state.value.selectedDate, tz
                )
                val items = RawHighlightScreenshotMapper.mapSessionsToScreenshotItems(
                    sessions, effectiveLocalDate, appInfoProvider, tz
                )
                val cached = items.mapNotNull { item ->
                    HighlightAnalysisCache.get(item.screenshotPath)?.let { item.screenshotPath to it }
                }.toMap()

                _state.update { current ->
                    val selected = if (current.selectedScreenshot != null && items.any { it.id == current.selectedScreenshot.id }) {
                        items.first { it.id == current.selectedScreenshot.id }
                    } else items.firstOrNull()
                    current.copy(selectedDate = effectiveDate, screenshots = items, selectedScreenshot = selected, analysisCache = cached + current.analysisCache)
                }

                if (items.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.Default) {
                        val reasonings = RawHighlightAnalysisExecutor.loadExistingReasonings(items, effectiveLocalDate.toString(), repository)
                        _state.update { it.copy(appDailyReasonings = it.appDailyReasonings + reasonings) }
                    }
                    val uncached = items.filterNot { HighlightAnalysisCache.contains(it.screenshotPath) }
                    viewModelScope.launch(Dispatchers.Default) {
                        uncached.forEach { if (!HighlightAnalysisCache.contains(it.screenshotPath)) executeAnalysis(it) }
                    }
                }
            }
        }
    }

    private fun selectScreenshot(item: RawHighlightScreenshotItem) {
        val cached = HighlightAnalysisCache.get(item.screenshotPath) ?: _state.value.analysisCache[item.screenshotPath]
        _state.update { it.copy(selectedScreenshot = item, currentResult = cached, error = null) }
        if (cached == null) analyzeScreenshot(item)
    }

    private fun analyzeScreenshot(item: RawHighlightScreenshotItem) {
        if (_state.value.analyzingPaths.contains(item.screenshotPath)) return
        viewModelScope.launch(Dispatchers.Default) { executeAnalysis(item) }
    }

    private suspend fun executeAnalysis(item: RawHighlightScreenshotItem) {
        _state.update { it.copy(isAnalyzing = true, analyzingPaths = it.analyzingPaths + item.screenshotPath) }
        try {
            analysisSemaphore.withPermit {
                val res = RawHighlightAnalysisExecutor.execute(item, visionService, repository, localHeuristicService, logger)
                _state.update { current ->
                    val remaining = current.analyzingPaths - item.screenshotPath
                    current.copy(
                        isAnalyzing = remaining.isNotEmpty(),
                        analyzingPaths = remaining,
                        analysisCache = if (res.analysis != null) current.analysisCache + (item.screenshotPath to res.analysis) else current.analysisCache,
                        localEntitiesCache = if (res.localEntities != null) current.localEntitiesCache + (item.screenshotPath to res.localEntities) else current.localEntitiesCache,
                        currentResult = if (current.selectedScreenshot?.screenshotPath == item.screenshotPath) res.analysis ?: current.currentResult else current.currentResult,
                        error = res.error ?: current.error
                    )
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error during executeAnalysis" }
        }
    }

    private fun generateDailyReasoning() {
        val date = _state.value.selectedDate ?: Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val localDate = date.toLocalDateTime(tz).date
        val yesterdayLocalDate = LocalDate.fromEpochDays(localDate.toEpochDays() - 1)
        val allItems = _state.value.screenshots.ifEmpty { listOfNotNull(_state.value.selectedScreenshot) }

        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(isReasoningLoading = true, reasoningStage = RawHighlightState.ReasoningStage.IDLE, reasoningProgress = 0f) }
            coordinator.runDailyReasoningPipeline(
                dateString = localDate.toString(),
                yesterdayDateString = yesterdayLocalDate.toString(),
                mode = _state.value.currentReasoningMode,
                allItems = allItems,
                onProgress = { p -> _state.update { it.copy(reasoningProgress = p) } },
                onStage = { s -> _state.update { it.copy(reasoningStage = s) } },
                onShieldActive = { _state.update { it.copy(isPrivacyShieldActive = true) } },
                onAppReasoningGenerated = { pkg, reasoning, icon -> _state.update { it.copy(appDailyReasonings = it.appDailyReasonings + (pkg to reasoning), categoryIcon = icon) } },
                onOverallReasoningGenerated = { overall -> _state.update { it.copy(appDailyReasonings = it.appDailyReasonings + ("ALL_APPS" to overall)) } }
            )
            _state.update { it.copy(isReasoningLoading = false) }
        }
    }

    private fun syncRealData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncingDeviceUsage = true) }
            deviceUsageSyncer.syncRealDeviceUsage()
            _state.update { it.copy(isSyncingDeviceUsage = false) }
            generateDailyReasoning()
        }
    }
}
