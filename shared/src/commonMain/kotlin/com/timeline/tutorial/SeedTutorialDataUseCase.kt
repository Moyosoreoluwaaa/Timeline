package com.timeline.tutorial

import com.timeline.data.TimelineRepository
import com.timeline.domain.Session
import kotlinx.coroutines.flow.first

class SeedTutorialDataUseCase(
    private val repository: TimelineRepository,
    private val mockDataSource: TutorialMockDataSource
) {
    /**
     * Seeds mock sessions if empty and returns the initial target session (middle item).
     */
    suspend operator fun invoke(): Session? {
        val currentSessions = repository.getTimeline().first()
        val sessionsToUse = if (currentSessions.isEmpty()) {
            val mockSessions = mockDataSource.getInitialTutorialSessions()
            repository.insertSessions(mockSessions)
            mockSessions
        } else {
            currentSessions
        }

        return if (sessionsToUse.isNotEmpty()) {
            val middleIndex = sessionsToUse.size / 2
            sessionsToUse[middleIndex]
        } else {
            null
        }
    }
}