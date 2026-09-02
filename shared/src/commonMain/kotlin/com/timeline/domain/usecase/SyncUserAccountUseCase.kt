package com.timeline.domain.usecase

import com.timeline.domain.SubscriptionManager
import com.timeline.domain.UserPreferences
import co.touchlab.kermit.Logger

class SyncUserAccountUseCase(
    private val subscriptionManager: SubscriptionManager,
    private val migrateGuestDataUseCase: MigrateGuestDataUseCase,
    private val userPreferences: UserPreferences
) {
    private val logger = Logger.withTag("SyncUserAccountUseCase")

    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            logger.i { "Starting account sync for user: $userId" }
            
            // 1. Sync with RevenueCat
            subscriptionManager.logIn(userId)
            
            // 2. Migrate local anonymous data
            migrateGuestDataUseCase(userId)

            // 3. Mark as logged in
            userPreferences.setLoggedIn(true)
            
            logger.i { "Account sync completed successfully" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(e) { "Account sync failed" }
            Result.failure(e)
        }
    }
}
