package com.timeline.domain

interface NotificationManager {
    fun initialize(appId: String)
    suspend fun login(externalId: String)
    suspend fun logout()
    suspend fun setEmail(email: String)
    suspend fun setSmsNumber(number: String)
    suspend fun setTag(key: String, value: String)
    fun hasPermission(): Boolean
}

class NoOpNotificationManager : NotificationManager {
    override fun initialize(appId: String) {}
    override suspend fun login(externalId: String) {}
    override suspend fun logout() {}
    override suspend fun setEmail(email: String) {}
    override suspend fun setSmsNumber(number: String) {}
    override suspend fun setTag(key: String, value: String) {}
    override fun hasPermission(): Boolean = false
}
