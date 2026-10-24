package com.timeline.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.timeline.domain.repository.AuthRepository
import com.timeline.util.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

interface ExclusionPolicy {
    fun isExcludedFlow(packageName: String): Flow<Boolean>
    suspend fun isExcluded(packageName: String): Boolean
    suspend fun toggleExclusion(packageName: String)
    fun getExcludedPackages(): Flow<Set<String>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineExclusionPolicy(
    private val dataStore: DataStore<Preferences>,
    private val authRepository: AuthRepository
) : ExclusionPolicy {
    private val hardcodedExclusions = Constants.HARDCODED_EXCLUSIONS

    override fun isExcludedFlow(packageName: String): Flow<Boolean> {
        return getExcludedPackages().map { userExclusions ->
            packageName in hardcodedExclusions || 
            packageName in userExclusions || 
            packageName.contains("launcher")
        }
    }

    override suspend fun isExcluded(packageName: String): Boolean {
        val userExclusions = getExcludedPackages().first()
        return packageName in hardcodedExclusions || 
               packageName in userExclusions || 
               packageName.contains("launcher")
    }

    override suspend fun toggleExclusion(packageName: String) {
        val userId = authRepository.getCurrentUser()?.uid
        val key = UserPreferenceKeys.excludedPackages(userId)
        dataStore.edit { prefs ->
            val current = prefs[key] ?: emptySet()
            if (packageName in current) {
                prefs[key] = current - packageName
            } else {
                prefs[key] = current + packageName
            }
        }
    }

    override fun getExcludedPackages(): Flow<Set<String>> {
        return authRepository.currentUser.flatMapLatest { user ->
            val key = UserPreferenceKeys.excludedPackages(user?.uid)
            dataStore.data.map { it[key] ?: emptySet() }
        }
    }
}
