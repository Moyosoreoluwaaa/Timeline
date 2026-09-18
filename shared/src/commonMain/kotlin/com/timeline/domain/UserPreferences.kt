package com.timeline.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.timeline.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

enum class MigrationStatus {
    IDLE,
    IN_PROGRESS,
    COMPLETED
}

data class MigrationState(
    val status: MigrationStatus = MigrationStatus.IDLE,
    val targetUserId: String? = null,
    val pathMap: Map<String, String> = emptyMap()
)

data class PreferencesState(
    val isPermissionsCompleted: Boolean,
    val isTutorial: Boolean,
    val isUsageTrackingEnabled: Boolean,
    val isScreenshotCaptureEnabled: Boolean,
    val isAiReasoningEnabled: Boolean,
    val dataRetentionDays: Int,
    val isLoggedIn: Boolean,
    val trialStartedAt: Long?, // null = not started
    val lastOnboardingStep: String
)

object UserPreferenceKeys {
    val IS_PERMISSIONS_COMPLETED = booleanPreferencesKey("is_permissions_completed")
    val IS_TUTORIAL_COMPLETED = booleanPreferencesKey("is_tutorial_completed")
    val IS_USAGE_TRACKING_ENABLED = booleanPreferencesKey("is_usage_tracking_enabled")
    val IS_SCREENSHOT_CAPTURE_ENABLED = booleanPreferencesKey("is_screenshot_capture_enabled")
    val IS_AI_REASONING_ENABLED = booleanPreferencesKey("is_ai_reasoning_enabled")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val MIGRATION_STATUS = stringPreferencesKey("migration_status")
    val MIGRATION_TARGET_USER_ID = stringPreferencesKey("migration_target_user_id")
    val MIGRATION_PATH_MAP = stringPreferencesKey("migration_path_map")

    fun trialStartedAt(userId: String?) =
        longPreferencesKey("${userId ?: "guest"}_trial_started_at")

    fun dataRetentionDays(userId: String?) =
        intPreferencesKey("${userId ?: "guest"}_data_retention_days")

    fun lastOnboardingStep(userId: String?) =
        stringPreferencesKey("${userId ?: "guest"}_last_onboarding_step")

    fun excludedPackages(userId: String?) =
        stringSetPreferencesKey("${userId ?: "guest"}_excluded_packages")
}

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferences(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository
) {
    val state: Flow<PreferencesState> = authRepository.currentUser.flatMapLatest { user ->
        val userId = user?.uid
        dataStore.data.map { prefs ->
            PreferencesState(
                isPermissionsCompleted = prefs[UserPreferenceKeys.IS_PERMISSIONS_COMPLETED]
                    ?: false,
                isUsageTrackingEnabled = prefs[UserPreferenceKeys.IS_USAGE_TRACKING_ENABLED]
                    ?: true,
                isScreenshotCaptureEnabled = prefs[UserPreferenceKeys.IS_SCREENSHOT_CAPTURE_ENABLED]
                    ?: true,
                isAiReasoningEnabled = prefs[UserPreferenceKeys.IS_AI_REASONING_ENABLED] ?: true,
                dataRetentionDays = prefs[UserPreferenceKeys.dataRetentionDays(userId)] ?: 30,
                isLoggedIn = prefs[UserPreferenceKeys.IS_LOGGED_IN] ?: false,
                trialStartedAt = prefs[UserPreferenceKeys.trialStartedAt(userId)],
                lastOnboardingStep = prefs[UserPreferenceKeys.lastOnboardingStep(userId)]
                    ?: "Welcome",
                isTutorial = prefs[UserPreferenceKeys.IS_TUTORIAL_COMPLETED] ?: false
            )
        }
    }

    suspend fun startTrial(timestamp: Long) {
        val userId = authRepository.getCurrentUser()?.uid
        dataStore.edit { it[UserPreferenceKeys.trialStartedAt(userId)] = timestamp }
    }

    suspend fun setPermissionsCompleted(completed: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_PERMISSIONS_COMPLETED] = completed }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_TUTORIAL_COMPLETED] = completed }
    }

    suspend fun setUsageTrackingEnabled(enabled: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_USAGE_TRACKING_ENABLED] = enabled }
    }

    suspend fun setScreenshotCaptureEnabled(enabled: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_SCREENSHOT_CAPTURE_ENABLED] = enabled }
    }

    suspend fun setAiReasoningEnabled(enabled: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_AI_REASONING_ENABLED] = enabled }
    }

    suspend fun setDataRetentionDays(days: Int) {
        val userId = authRepository.getCurrentUser()?.uid
        dataStore.edit { it[UserPreferenceKeys.dataRetentionDays(userId)] = days }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_LOGGED_IN] = loggedIn }
    }

    suspend fun setLastOnboardingStep(step: String) {
        val userId = authRepository.getCurrentUser()?.uid
        dataStore.edit { it[UserPreferenceKeys.lastOnboardingStep(userId)] = step }
    }

    suspend fun getMigrationState(): MigrationState {
        val prefs = dataStore.data.first()
        val statusStr = prefs[UserPreferenceKeys.MIGRATION_STATUS] ?: MigrationStatus.IDLE.name
        val status = try {
            MigrationStatus.valueOf(statusStr)
        } catch (_: Exception) {
            MigrationStatus.IDLE
        }
        val targetUserId = prefs[UserPreferenceKeys.MIGRATION_TARGET_USER_ID]
        val pathMapRaw = prefs[UserPreferenceKeys.MIGRATION_PATH_MAP] ?: ""
        val pathMap = deserializePathMap(pathMapRaw)
        return MigrationState(status, targetUserId, pathMap)
    }

    suspend fun setMigrationState(
        status: MigrationStatus,
        targetUserId: String?,
        pathMap: Map<String, String>
    ) {
        dataStore.edit { prefs ->
            prefs[UserPreferenceKeys.MIGRATION_STATUS] = status.name
            if (targetUserId != null) {
                prefs[UserPreferenceKeys.MIGRATION_TARGET_USER_ID] = targetUserId
            } else {
                prefs.remove(UserPreferenceKeys.MIGRATION_TARGET_USER_ID)
            }
            prefs[UserPreferenceKeys.MIGRATION_PATH_MAP] = serializePathMap(pathMap)
        }
    }

    suspend fun clearMigrationState() {
        dataStore.edit { prefs ->
            prefs.remove(UserPreferenceKeys.MIGRATION_STATUS)
            prefs.remove(UserPreferenceKeys.MIGRATION_TARGET_USER_ID)
            prefs.remove(UserPreferenceKeys.MIGRATION_PATH_MAP)
        }
    }

    private fun serializePathMap(pathMap: Map<String, String>): String {
        return pathMap.entries.joinToString("\n") { "${it.key}\t${it.value}" }
    }

    private fun deserializePathMap(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.split("\n")
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("\t")
                if (parts.size >= 2) parts[0] to parts[1] else null
            }.toMap()
    }
}
