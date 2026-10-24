package com.timeline.domain

interface DeviceUsageSyncer {
    suspend fun syncRealDeviceUsage(daysBack: Int = 1): Int
    fun hasPermission(): Boolean
}

class NoOpDeviceUsageSyncer : DeviceUsageSyncer {
    override suspend fun syncRealDeviceUsage(daysBack: Int): Int = 0
    override fun hasPermission(): Boolean = true
}
