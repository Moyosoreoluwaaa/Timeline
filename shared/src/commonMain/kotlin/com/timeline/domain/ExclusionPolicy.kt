package com.timeline.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface ExclusionPolicy {
    fun isExcludedFlow(packageName: String): Flow<Boolean>
    suspend fun isExcluded(packageName: String): Boolean
    suspend fun toggleExclusion(packageName: String)
    fun getExcludedPackages(): Flow<Set<String>>
}

class TimelineExclusionPolicy(
    private val dataStore: DataStore<Preferences>
) : ExclusionPolicy {
    private val EXCLUDED_PACKAGES_KEY = stringSetPreferencesKey("excluded_packages")

    private val hardcodedExclusions = setOf(
        "com.timeline",
        "android",
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3",
        "com.transsion.XOSLauncher" // Added as per mockup observing launcher in focus
    )

    override fun isExcludedFlow(packageName: String): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            val userExclusions = prefs[EXCLUDED_PACKAGES_KEY] ?: emptySet()
            packageName in hardcodedExclusions || 
            packageName in userExclusions || 
            packageName.contains("launcher")
        }
    }

    override suspend fun isExcluded(packageName: String): Boolean {
        val prefs = dataStore.data.first()
        val userExclusions = prefs[EXCLUDED_PACKAGES_KEY] ?: emptySet()
        return packageName in hardcodedExclusions || 
               packageName in userExclusions || 
               packageName.contains("launcher")
    }

    override suspend fun toggleExclusion(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[EXCLUDED_PACKAGES_KEY] ?: emptySet()
            if (packageName in current) {
                prefs[EXCLUDED_PACKAGES_KEY] = current - packageName
            } else {
                prefs[EXCLUDED_PACKAGES_KEY] = current + packageName
            }
        }
    }

    override fun getExcludedPackages(): Flow<Set<String>> {
        return dataStore.data.map { it[EXCLUDED_PACKAGES_KEY] ?: emptySet() }
    }
}
