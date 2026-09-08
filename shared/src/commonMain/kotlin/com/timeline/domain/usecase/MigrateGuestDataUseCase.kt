package com.timeline.domain.usecase

import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.data.UserStorageManager
import com.timeline.domain.MigrationStatus
import com.timeline.domain.UserPreferences

class MigrateGuestDataUseCase(
    private val timelineRepository: TimelineRepository,
    private val userStorageManager: UserStorageManager,
    private val userPreferences: UserPreferences,
    private val logger: Logger
) {
    private val tagLogger = logger.withTag("MigrateGuestDataUseCase")

    suspend operator fun invoke(userId: String): Result<Unit> = runCatching {
        tagLogger.i { "Starting guest data migration for target userId: $userId" }
        val migrationState = userPreferences.getMigrationState()

        // 1. Check for stale target account
        if (migrationState.status == MigrationStatus.IN_PROGRESS && migrationState.targetUserId != userId) {
            tagLogger.w { "Found stale migration for ${migrationState.targetUserId}, resetting state" }
            userPreferences.clearMigrationState()
        }

        // 2. Obtain path mapping (reuse saved map if resuming, otherwise copy files)
        val pathMap = if (migrationState.status == MigrationStatus.IN_PROGRESS &&
            migrationState.targetUserId == userId &&
            migrationState.pathMap.isNotEmpty()
        ) {
            tagLogger.i { "Resuming migration with existing pathMap (${migrationState.pathMap.size} files)" }
            migrationState.pathMap
        } else {
            userPreferences.setMigrationState(MigrationStatus.IN_PROGRESS, userId, emptyMap())
            val copied = userStorageManager.copyGuestScreenshotsToUser(userId)
            tagLogger.i { "Copied ${copied.size} screenshots to user directory" }
            userPreferences.setMigrationState(MigrationStatus.IN_PROGRESS, userId, copied)
            copied
        }

        // 3. Atomic Database Transaction
        tagLogger.i { "Migrating guest sessions in Room database" }
        timelineRepository.migrateGuestSessions(userId, pathMap)

        // 4. Cleanup guest files & mark completed
        tagLogger.i { "Cleaning up guest files and finalizing migration state" }
        userStorageManager.cleanupGuestScreenshots()
        userPreferences.clearMigrationState()
        tagLogger.i { "Guest data migration finished successfully" }
    }.onFailure { e ->
        tagLogger.e(e) { "Guest data migration failed" }
    }
}
