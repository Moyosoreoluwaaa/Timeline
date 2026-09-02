package com.timeline.domain.usecase

import com.timeline.data.TimelineRepository

class MigrateGuestDataUseCase(
    private val timelineRepository: TimelineRepository
) {
    suspend operator fun invoke(userId: String) {
        timelineRepository.associateAnonymousSessions(userId)
    }
}
