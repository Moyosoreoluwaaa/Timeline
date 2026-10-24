package com.timeline.domain.ml

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton in-memory cache for processed vision analysis results.
 * Prevents redundant OCR execution, removes duplicate spinners,
 * and enables instant preloading across screens and ViewModel lifecycles.
 */
object HighlightAnalysisCache {
    private val _cache = MutableStateFlow<Map<String, ImageAnalysisResult>>(emptyMap())
    val cache: StateFlow<Map<String, ImageAnalysisResult>> = _cache.asStateFlow()

    fun get(path: String): ImageAnalysisResult? = _cache.value[path]

    fun put(path: String, result: ImageAnalysisResult) {
        _cache.update { it + (path to result) }
    }

    fun getAll(): Map<String, ImageAnalysisResult> = _cache.value

    fun contains(path: String): Boolean = _cache.value.containsKey(path)

    fun clear() {
        _cache.value = emptyMap()
    }
}
