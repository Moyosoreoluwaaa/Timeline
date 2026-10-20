package com.timeline.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.timeline.domain.reasoning.HighlightReasoningMode
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
    val isAgentReasoningEnabled: Boolean,
    val highlightReasoningMode: HighlightReasoningMode = HighlightReasoningMode.BALANCED,
    val digestFrequency: Int = 1,
    val digestHours: List<Int> = listOf(12, 17, 21),
    val dataRetentionDays: Int,
    val isLoggedIn: Boolean,
    val trialStartedAt: Long?, // null = not started
    val lastOnboardingStep: String,
    val isAnalyticsEnabled: Boolean = true,
    val hasTrackingStarted: Boolean = false,
    val focusAreas: Set<String> = emptySet(),
    val experiments: com.timeline.domain.model.ExperimentFlags = com.timeline.domain.model.ExperimentFlags()
)

object UserPreferenceKeys {
    val EXPERIMENT_ONBOARDING_VARIANT = stringPreferencesKey("exp_onboarding_variant")
    val EXPERIMENT_PAYWALL_STYLE = stringPreferencesKey("exp_paywall_style")
    val EXPERIMENT_HIGHLIGHT_VARIANT = stringPreferencesKey("exp_highlight_variant")
    val EXPERIMENT_HIGHLIGHT_LOADING_STYLE = stringPreferencesKey("exp_highlight_loading_style")
    val FOCUS_AREAS = stringSetPreferencesKey("focus_areas")
    val IS_PERMISSIONS_COMPLETED = booleanPreferencesKey("is_permissions_completed")
    val IS_TUTORIAL_COMPLETED = booleanPreferencesKey("is_tutorial_completed")
    val IS_USAGE_TRACKING_ENABLED = booleanPreferencesKey("is_usage_tracking_enabled")
    val IS_SCREENSHOT_CAPTURE_ENABLED = booleanPreferencesKey("is_screenshot_capture_enabled")
    val IS_ANALYTICS_ENABLED = booleanPreferencesKey("is_analytics_enabled")
    val HAS_TRACKING_STARTED = booleanPreferencesKey("has_tracking_started")
    val IS_AI_REASONING_ENABLED = booleanPreferencesKey("is_ai_reasoning_enabled")
    val HIGHLIGHT_REASONING_MODE = stringPreferencesKey("highlight_reasoning_mode")
    val DIGEST_FREQUENCY = intPreferencesKey("digest_frequency")
    val DIGEST_HOURS = stringPreferencesKey("digest_hours")
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
            val modeStr = prefs[UserPreferenceKeys.HIGHLIGHT_REASONING_MODE]
            val reasoningMode = modeStr?.let {
                try {
                    HighlightReasoningMode.valueOf(it)
                } catch (_: Exception) {
                    HighlightReasoningMode.BALANCED
                }
            } ?: HighlightReasoningMode.BALANCED

            val frequency = prefs[UserPreferenceKeys.DIGEST_FREQUENCY] ?: 3
            val hoursStr = prefs[UserPreferenceKeys.DIGEST_HOURS]
            val hoursList = hoursStr?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it in 0..23 }
                ?: listOf(12, 17, 21)

            PreferencesState(
                isPermissionsCompleted = prefs[UserPreferenceKeys.IS_PERMISSIONS_COMPLETED]
                    ?: false,
                isUsageTrackingEnabled = prefs[UserPreferenceKeys.IS_USAGE_TRACKING_ENABLED]
                    ?: true,
                isScreenshotCaptureEnabled = prefs[UserPreferenceKeys.IS_SCREENSHOT_CAPTURE_ENABLED]
                    ?: true,
                isAgentReasoningEnabled = prefs[UserPreferenceKeys.IS_AI_REASONING_ENABLED] ?: true,
                highlightReasoningMode = reasoningMode,
                digestFrequency = frequency,
                digestHours = hoursList,
                dataRetentionDays = prefs[UserPreferenceKeys.dataRetentionDays(userId)] ?: 7,
                isLoggedIn = prefs[UserPreferenceKeys.IS_LOGGED_IN] ?: false,
                trialStartedAt = prefs[UserPreferenceKeys.trialStartedAt(userId)],
                lastOnboardingStep = prefs[UserPreferenceKeys.lastOnboardingStep(userId)]
                    ?: "Opening",
                isAnalyticsEnabled = prefs[UserPreferenceKeys.IS_ANALYTICS_ENABLED] ?: true,
                hasTrackingStarted = prefs[UserPreferenceKeys.HAS_TRACKING_STARTED] ?: false,
                isTutorial = prefs[UserPreferenceKeys.IS_TUTORIAL_COMPLETED] ?: false,
                focusAreas = prefs[UserPreferenceKeys.FOCUS_AREAS] ?: emptySet(),
                experiments = com.timeline.domain.model.ExperimentFlags(
                    onboardingVariant = prefs[UserPreferenceKeys.EXPERIMENT_ONBOARDING_VARIANT]?.let {
                        try { com.timeline.ui.components.FanVariant.valueOf(it) } catch (_: Exception) { com.timeline.ui.components.FanVariant.CenteredFan }
                    } ?: com.timeline.ui.components.FanVariant.CenteredFan,
                    paywallStyle = prefs[UserPreferenceKeys.EXPERIMENT_PAYWALL_STYLE]?.let {
                        try { com.timeline.ui.paywall.NewPaywallStyle.valueOf(it) } catch (_: Exception) { com.timeline.ui.paywall.NewPaywallStyle.Classic }
                    } ?: com.timeline.ui.paywall.NewPaywallStyle.Classic,
                    highlightVariant = prefs[UserPreferenceKeys.EXPERIMENT_HIGHLIGHT_VARIANT]?.let {
                        try { com.timeline.domain.model.HighlightVariant.valueOf(it) } catch (_: Exception) { com.timeline.domain.model.HighlightVariant.Simplified }
                    } ?: com.timeline.domain.model.HighlightVariant.Simplified,
                    highlightLoadingStyle = prefs[UserPreferenceKeys.EXPERIMENT_HIGHLIGHT_LOADING_STYLE]?.let {
                        try { com.timeline.domain.model.HighlightLoadingStyle.valueOf(it) } catch (_: Exception) { com.timeline.domain.model.HighlightLoadingStyle.CardStack }
                    } ?: com.timeline.domain.model.HighlightLoadingStyle.CardStack
                )
            )
        }
    }

    suspend fun setExperimentOnboardingVariant(variant: com.timeline.ui.components.FanVariant) {
        dataStore.edit { it[UserPreferenceKeys.EXPERIMENT_ONBOARDING_VARIANT] = variant.name }
    }

    suspend fun setExperimentPaywallStyle(style: com.timeline.ui.paywall.NewPaywallStyle) {
        dataStore.edit { it[UserPreferenceKeys.EXPERIMENT_PAYWALL_STYLE] = style.name }
    }

    suspend fun setExperimentHighlightVariant(variant: com.timeline.domain.model.HighlightVariant) {
        dataStore.edit { it[UserPreferenceKeys.EXPERIMENT_HIGHLIGHT_VARIANT] = variant.name }
    }

    suspend fun setExperimentHighlightLoadingStyle(style: com.timeline.domain.model.HighlightLoadingStyle) {
        dataStore.edit { it[UserPreferenceKeys.EXPERIMENT_HIGHLIGHT_LOADING_STYLE] = style.name }
    }

    suspend fun setHighlightReasoningMode(mode: HighlightReasoningMode) {
        dataStore.edit { it[UserPreferenceKeys.HIGHLIGHT_REASONING_MODE] = mode.name }
    }

    suspend fun setDigestSchedule(frequency: Int, hours: List<Int>) {
        dataStore.edit {
            it[UserPreferenceKeys.DIGEST_FREQUENCY] = frequency.coerceIn(1, 6)
            it[UserPreferenceKeys.DIGEST_HOURS] = hours.take(6).joinToString(",")
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

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_ANALYTICS_ENABLED] = enabled }
    }

    suspend fun setTrackingStarted(started: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.HAS_TRACKING_STARTED] = started }
    }

    suspend fun setAgentReasoningEnabled(enabled: Boolean) {
        dataStore.edit { it[UserPreferenceKeys.IS_AI_REASONING_ENABLED] = enabled }
    }

    suspend fun setFocusAreas(areas: Set<String>) {
        dataStore.edit { it[UserPreferenceKeys.FOCUS_AREAS] = areas }
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
