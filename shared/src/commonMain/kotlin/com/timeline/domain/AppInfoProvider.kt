package com.timeline.domain

interface AppInfoProvider {
    suspend fun getAppName(packageName: String): String
    suspend fun getAppIcon(packageName: String): Any? // Platform specific icon
    suspend fun getInstalledApps(): List<AppMetadata>
}

data class AppMetadata(
    val packageName: String,
    val name: String,
    val icon: Any? = null
)

class NoOpAppInfoProvider : AppInfoProvider {
    override suspend fun getAppName(packageName: String): String = packageName
    override suspend fun getAppIcon(packageName: String): Any? = null
    override suspend fun getInstalledApps(): List<AppMetadata> = emptyList()
}
