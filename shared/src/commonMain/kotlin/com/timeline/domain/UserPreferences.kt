package com.timeline.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PreferencesState(
    val isPermissionsCompleted: Boolean,
    val isUsageTrackingEnabled: Boolean,
    val isScreenshotCaptureEnabled: Boolean,
    val dataRetentionDays: Int
)

class UserPreferences(private val dataStore: DataStore<Preferences>) {
    private val IS_PERMISSIONS_COMPLETED = booleanPreferencesKey("is_permissions_completed")
    private val IS_USAGE_TRACKING_ENABLED = booleanPreferencesKey("is_usage_tracking_enabled")
    private val IS_SCREENSHOT_CAPTURE_ENABLED = booleanPreferencesKey("is_screenshot_capture_enabled")
    private val DATA_RETENTION_DAYS = intPreferencesKey("data_retention_days")

    val state: Flow<PreferencesState> = dataStore.data.map { prefs ->
        PreferencesState(
            isPermissionsCompleted = prefs[IS_PERMISSIONS_COMPLETED] ?: false,
            isUsageTrackingEnabled = prefs[IS_USAGE_TRACKING_ENABLED] ?: true,
            isScreenshotCaptureEnabled = prefs[IS_SCREENSHOT_CAPTURE_ENABLED] ?: true,
            dataRetentionDays = prefs[DATA_RETENTION_DAYS] ?: 30
        )
    }

    suspend fun setPermissionsCompleted(completed: Boolean) {
        dataStore.edit { it[IS_PERMISSIONS_COMPLETED] = completed }
    }

    suspend fun setUsageTrackingEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_USAGE_TRACKING_ENABLED] = enabled }
    }

    suspend fun setScreenshotCaptureEnabled(enabled: Boolean) {
        dataStore.edit { it[IS_SCREENSHOT_CAPTURE_ENABLED] = enabled }
    }

    suspend fun setDataRetentionDays(days: Int) {
        dataStore.edit { it[DATA_RETENTION_DAYS] = days }
    }
}
