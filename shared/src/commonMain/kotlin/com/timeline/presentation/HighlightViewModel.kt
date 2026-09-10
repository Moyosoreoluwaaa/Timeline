package com.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.ml.HighlightAnalysisCache
import com.timeline.domain.ml.VisionAnalysisService
import com.timeline.domain.reasoning.*
import com.timeline.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class HighlightViewModel(
    private val repository: TimelineRepository,
    private val appInfoProvider: com.timeline.domain.AppInfoProvider,
    private val visionService: VisionAnalysisService,
    private val reasoningService: ReasoningService,
    private val localHeuristicService: LocalHeuristicService,
    private val userPreferences: UserPreferences,
    private val subscriptionManager: SubscriptionManager,
    private val deviceUsageSyncer: DeviceUsageSyncer,
    private val logger: Logger
) : ViewModel() {
    private val analysisSemaphore = Semaphore(2)
    private val _state = MutableStateFlow(
        HighlightState(
            analysisCache = HighlightAnalysisCache.getAll(),
            isUsagePermissionGranted = deviceUsageSyncer.hasPermission()
        )
    )
    val state: StateFlow<HighlightState> = _state.asStateFlow()

    private val _effect = Channel<HighlightEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var timelineJob: Job? = null

    init {
        loadScreenshots()
        observePreferences()
        
        // Background check for real usage once on startup
        viewModelScope.launch {
            val hasPerm = deviceUsageSyncer.hasPermission()
            _state.update { it.copy(isUsagePermissionGranted = hasPerm) }
        }
    }

    private fun syncRealData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncingDeviceUsage = true) }
            val count = deviceUsageSyncer.syncRealDeviceUsage(daysBack = 2)
            logger.i { "syncRealDeviceUsage finished with $count sessions" }

            // Ensure AI reasoning is active for Gemini narratives
            userPreferences.setAiReasoningEnabled(true)
            _state.update { it.copy(isAiOptedIn = true, isSyncingDeviceUsage = false) }
            
            // repository.getTimeline() flow will automatically trigger update via loadScreenshots job
            generateDailyReasoning()
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.state.collect { prefs ->
                _state.update { it.copy(isAiOptedIn = prefs.isAiReasoningEnabled) }
            }
        }
        viewModelScope.launch {
            subscriptionManager.isPro.collect { isPro ->
                _state.update { it.copy(isPro = isPro) }
            }
        }
    }

    fun onEvent(event: HighlightEvent) {
        when (event) {
            is HighlightEvent.Refresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    val count = deviceUsageSyncer.syncRealDeviceUsage(daysBack = 1)
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
            is HighlightEvent.SyncRealData -> syncRealData()
            is HighlightEvent.LoadScreenshots -> loadScreenshots()
            is HighlightEvent.SelectScreenshot -> selectScreenshot(event.item)
            is HighlightEvent.AnalyzeScreenshot -> analyzeScreenshot(event.item)
            is HighlightEvent.UpdateSearchQuery -> _state.update { it.copy(searchQuery = event.query) }
            is HighlightEvent.ClearSearch -> _state.update { it.copy(searchQuery = "") }
            is HighlightEvent.TogglePackageFilter -> {
                _state.update { it.copy(selectedPackage = event.packageName) }
                // Re-select first item in filtered list if current selected is not in filtered list
                val currentState = _state.value
                val filtered = currentState.filteredScreenshots
                if (currentState.selectedScreenshot !in filtered) {
                    filtered.firstOrNull()?.let { selectScreenshot(it) }
                }
            }
            is HighlightEvent.SelectDate -> {
                _state.update { it.copy(selectedDate = event.date) }
                loadScreenshots()
            }
            is HighlightEvent.ToggleAiOptIn -> {
                viewModelScope.launch {
                    val current = _state.value.isAiOptedIn
                    userPreferences.setAiReasoningEnabled(!current)
                }
            }
            is HighlightEvent.GenerateReasoning -> generateDailyReasoning()
        }
    }

    private fun generateDailyReasoning() {
        if (!_state.value.isAiOptedIn) {
            viewModelScope.launch {
                userPreferences.setAiReasoningEnabled(true)
            }
            _state.update { it.copy(isAiOptedIn = true) }
        }
        
        val date = _state.value.selectedDate ?: Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val localDate = date.toLocalDateTime(tz).date
        val dateString = localDate.toString() // YYYY-MM-DD

        val allItems = if (_state.value.screenshots.isNotEmpty()) {
            _state.value.screenshots
        } else {
            listOfNotNull(_state.value.selectedScreenshot)
        }
        val groupedByApp = allItems.groupBy { it.packageName }
        
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(isReasoningLoading = true) }
            
            groupedByApp.forEach { (packageName, items) ->
                if (PrivacyExclusionProvider.isPackageExcluded(packageName)) return@forEach
                
                // Collect OCR results for this app/day
                val ocrDumps = items.mapNotNull { _state.value.analysisCache[it.screenshotPath]?.textResult?.fullText }
                val labels = items.flatMap { item -> 
                    _state.value.analysisCache[item.screenshotPath]?.labels?.map { it.text } ?: emptyList() 
                }
                
                // If OCR cache is not ready yet, perform immediate analysis
                val effectiveDumps = if (ocrDumps.isEmpty()) {
                    items.mapNotNull { item ->
                        visionService.analyzeImageFromPath(item.screenshotPath).getOrNull()?.let { analysis ->
                            HighlightAnalysisCache.put(item.screenshotPath, analysis)
                            val localEntities = localHeuristicService.performLocalHeuristics(listOf(analysis.textResult.fullText))
                            _state.update { current ->
                                current.copy(
                                    analysisCache = current.analysisCache + (item.screenshotPath to analysis),
                                    localEntitiesCache = current.localEntitiesCache + (item.screenshotPath to localEntities),
                                    currentResult = if (current.selectedScreenshot?.screenshotPath == item.screenshotPath) analysis else current.currentResult
                                )
                            }
                            analysis.textResult.fullText
                        }
                    }
                } else {
                    ocrDumps
                }

                val appName = items.firstOrNull()?.displayName ?: appInfoProvider.getAppName(packageName) ?: packageName
                val validDumps = if (effectiveDumps.isNotEmpty()) effectiveDumps else listOf("Active usage session in $appName")
                
                logger.d { "Generating daily narrative for $appName on $dateString" }
                val result = reasoningService.generateDailyNarrative(
                    packageName = packageName,
                    appName = appName,
                    date = dateString,
                    ocrDumps = validDumps,
                    labels = labels
                )
                
                result.onSuccess { reasoning ->
                    repository.saveAppDailyReasoning(reasoning)
                    _state.update { it.copy(
                        appDailyReasonings = it.appDailyReasonings + (packageName to reasoning)
                    ) }
                }.onFailure { err ->
                    logger.w(err) { "Reasoning failed for $packageName, applying local fallback" }
                    val fallbackReasoning = LocalHeuristicService().generateDailyNarrative(
                        packageName = packageName,
                        appName = appName,
                        date = dateString,
                        ocrDumps = validDumps,
                        labels = labels
                    ).getOrNull()
                    if (fallbackReasoning != null) {
                        repository.saveAppDailyReasoning(fallbackReasoning)
                        _state.update { it.copy(
                            appDailyReasonings = it.appDailyReasonings + (packageName to fallbackReasoning)
                        ) }
                    }
                }
            }

            // Generate daily executive narrative for all apps combined
            if (groupedByApp.isNotEmpty()) {
                val appNames = groupedByApp.keys.mapNotNull { pkg ->
                    allItems.firstOrNull { it.packageName == pkg }?.displayName ?: appInfoProvider.getAppName(pkg) ?: pkg
                }.distinct().joinToString(", ")

                val topDumps = groupedByApp.values.flatMap { it.take(2) }.mapNotNull {
                    _state.value.analysisCache[it.screenshotPath]?.textResult?.fullText
                }.take(6)

                val allLabels = groupedByApp.values.flatMap { it }.flatMap { item ->
                    _state.value.analysisCache[item.screenshotPath]?.labels?.map { it.text } ?: emptyList()
                }.distinct().take(10)

                val promptDumps = if (topDumps.isNotEmpty()) topDumps else listOf("Active real device sessions recorded today across: $appNames")

                val overallResult = reasoningService.generateDailyNarrative(
                    packageName = "ALL_APPS",
                    appName = "Daily Summary ($appNames)",
                    date = dateString,
                    ocrDumps = promptDumps,
                    labels = allLabels
                )
                overallResult.onSuccess { overallReasoning ->
                    repository.saveAppDailyReasoning(overallReasoning)
                    _state.update { it.copy(
                        appDailyReasonings = it.appDailyReasonings + ("ALL_APPS" to overallReasoning)
                    ) }
                }.onFailure {
                    val fallback = LocalHeuristicService().generateDailyNarrative(
                        packageName = "ALL_APPS",
                        appName = "Daily Summary ($appNames)",
                        date = dateString,
                        ocrDumps = promptDumps,
                        labels = allLabels
                    ).getOrNull()
                    if (fallback != null) {
                        repository.saveAppDailyReasoning(fallback)
                        _state.update { it.copy(
                            appDailyReasonings = it.appDailyReasonings + ("ALL_APPS" to fallback)
                        ) }
                    }
                }
            }
            
            _state.update { it.copy(isReasoningLoading = false) }
        }
    }

    private fun loadScreenshots() {
        timelineJob?.cancel()
        timelineJob = viewModelScope.launch {
            repository.getTimeline().collect { sessions ->
                var selectedDate = _state.value.selectedDate ?: Clock.System.now()
                val tz = TimeZone.currentSystemDefault()
                var selectedLocalDate = selectedDate.toLocalDateTime(tz).date
                
                println("DEBUG: HighlightViewModel: loadScreenshots got ${sessions.size} sessions")

                // If no screenshots exist for the current selected date (and it's today),
                // auto-select the date of the latest session in the timeline so we show some content!
                val todayLocalDate = Clock.System.now().toLocalDateTime(tz).date
                val sessionsForSelectedDate = sessions.filter { it.startTime.toLocalDateTime(tz).date == selectedLocalDate }
                if (sessionsForSelectedDate.isEmpty() && sessions.isNotEmpty() && selectedLocalDate == todayLocalDate) {
                    val latestSession = sessions.maxByOrNull { it.startTime }
                    if (latestSession != null) {
                        selectedDate = latestSession.startTime
                        selectedLocalDate = selectedDate.toLocalDateTime(tz).date
                        println("DEBUG: HighlightViewModel: Auto-selected latest session date $selectedLocalDate")
                    }
                }

                val items = mutableListOf<HighlightScreenshotItem>()
                val filteredSessions = sessions.filter { it.startTime.toLocalDateTime(tz).date == selectedLocalDate }
                
                for (session in filteredSessions) {
                    val displayName = session.displayName ?: appInfoProvider.getAppName(session.packageName)
                    val icon = session.icon ?: appInfoProvider.getAppIcon(session.packageName)
                    
                    val paths = (session.screenshots + session.segments.mapNotNull { it.screenshotPath }).distinct()
                    paths.forEachIndexed { index, path ->
                        items.add(
                            HighlightScreenshotItem(
                                id = "${session.id}_$index",
                                sessionId = session.id,
                                packageName = session.packageName,
                                displayName = displayName,
                                icon = icon,
                                screenshotPath = path,
                                timestamp = session.startTime,
                                durationMinutes = session.durationMinutes
                            )
                        )
                    }
                }

                println("DEBUG: HighlightViewModel: Updating state with ${items.size} items for $selectedLocalDate")
                val cached = HighlightAnalysisCache.getAll()
                _state.update { current ->
                    val selected = if (current.selectedScreenshot != null && items.any { it.id == current.selectedScreenshot.id }) {
                        current.selectedScreenshot
                    } else {
                        items.firstOrNull()
                    }
                    current.copy(
                        selectedDate = selectedDate,
                        screenshots = items,
                        selectedScreenshot = selected,
                        analysisCache = cached + current.analysisCache
                    )
                }

                if (items.isNotEmpty()) {
                    // Non-blocking load of existing reasonings
                    loadExistingReasonings(items, selectedLocalDate.toString())
                    // Automatically preload any uncached screenshots in background
                    preloadScreenshots(items)
                }
            }
        }
    }

    private fun loadExistingReasonings(items: List<HighlightScreenshotItem>, dateString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val uniquePackages = (items.map { it.packageName } + "ALL_APPS").distinct()
            uniquePackages.forEach { pkg ->
                repository.getAppDailyReasoning(pkg, dateString).firstOrNull()?.let { reasoning ->
                    _state.update { it.copy(
                        appDailyReasonings = it.appDailyReasonings + (pkg to reasoning)
                    ) }
                }
            }
        }
    }

    private fun preloadScreenshots(items: List<HighlightScreenshotItem>) {
        val uncached = items.filterNot { HighlightAnalysisCache.contains(it.screenshotPath) }
        if (uncached.isEmpty()) return

        // Prioritize newest items first so visible cards are processed first
        val prioritized = uncached.sortedByDescending { it.timestamp }

        viewModelScope.launch(Dispatchers.Default) {
            prioritized.forEach { item ->
                if (!HighlightAnalysisCache.contains(item.screenshotPath)) {
                    executeAnalysis(item)
                }
            }
        }
    }

    private fun selectScreenshot(item: HighlightScreenshotItem) {
        val cachedResult = HighlightAnalysisCache.get(item.screenshotPath) ?: _state.value.analysisCache[item.screenshotPath]
        _state.update {
            it.copy(
                selectedScreenshot = item,
                currentResult = cachedResult,
                error = null
            )
        }
        if (cachedResult == null) {
            analyzeScreenshot(item)
        }
    }

    private fun analyzeScreenshot(item: HighlightScreenshotItem) {
        if (HighlightAnalysisCache.contains(item.screenshotPath)) {
            val cached = HighlightAnalysisCache.get(item.screenshotPath)
            if (cached != null) {
                _state.update { current ->
                    current.copy(
                        analysisCache = current.analysisCache + (item.screenshotPath to cached),
                        currentResult = if (current.selectedScreenshot?.screenshotPath == item.screenshotPath) cached else current.currentResult
                    )
                }
            }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            executeAnalysis(item)
        }
    }

    private suspend fun executeAnalysis(item: HighlightScreenshotItem) {
        if (HighlightAnalysisCache.contains(item.screenshotPath)) return
        if (_state.value.analyzingPaths.contains(item.screenshotPath)) return

        analysisSemaphore.withPermit {
            if (HighlightAnalysisCache.contains(item.screenshotPath)) return@withPermit
            
            _state.update { 
                it.copy(
                    isAnalyzing = true, 
                    analyzingPaths = it.analyzingPaths + item.screenshotPath,
                    error = null
                ) 
            }

            try {
                logger.d { "Running background ML Kit OCR on: ${item.screenshotPath}" }
                val result = visionService.analyzeImageFromPath(item.screenshotPath)
                result.onSuccess { analysis ->
                    HighlightAnalysisCache.put(item.screenshotPath, analysis)
                    val localEntities = localHeuristicService.performLocalHeuristics(listOf(analysis.textResult.fullText))
                    
                    _state.update { current ->
                        val updatedCache = current.analysisCache + (item.screenshotPath to analysis)
                        val updatedEntities = current.localEntitiesCache + (item.screenshotPath to localEntities)
                        val updatedAnalyzing = current.analyzingPaths - item.screenshotPath
                        current.copy(
                            isAnalyzing = updatedAnalyzing.isNotEmpty(),
                            analyzingPaths = updatedAnalyzing,
                            analysisCache = updatedCache,
                            localEntitiesCache = updatedEntities,
                            currentResult = if (current.selectedScreenshot?.screenshotPath == item.screenshotPath) analysis else current.currentResult,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    logger.e(error) { "Failed to analyze image with ML Kit" }
                    _state.update { current ->
                        val updatedAnalyzing = current.analyzingPaths - item.screenshotPath
                        current.copy(
                            isAnalyzing = updatedAnalyzing.isNotEmpty(),
                            analyzingPaths = updatedAnalyzing,
                            error = if (current.selectedScreenshot?.screenshotPath == item.screenshotPath) 
                                (error.message ?: "Analysis failed") else current.error
                        )
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Unexpected analysis error" }
                _state.update { current ->
                    val updatedAnalyzing = current.analyzingPaths - item.screenshotPath
                    current.copy(
                        isAnalyzing = updatedAnalyzing.isNotEmpty(),
                        analyzingPaths = updatedAnalyzing
                    )
                }
            }
        }
    }


}
