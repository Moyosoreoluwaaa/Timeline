package com.timeline.domain.repository

import com.timeline.domain.model.ReelSegment
import com.timeline.domain.model.ReelSlide
import com.timeline.presentation.TimeOfDayFilter
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the reel-style highlight experience.
 *
 * [getDaySegments] provides a live stream of today's time-of-day segments for the card list.
 * [getReelSlides] returns the slide sequence for a specific time window on demand when the user
 * opens a reel.
 * [syncRealDeviceUsage] delegates to the platform usage syncer.
 */
interface ReelHighlightRepository {
    fun getDaySegments(): Flow<List<ReelSegment>>
    suspend fun getReelSlides(filter: TimeOfDayFilter): List<ReelSlide>
    suspend fun syncRealDeviceUsage()
}
