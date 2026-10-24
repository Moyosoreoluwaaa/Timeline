package com.timeline.domain

interface AppInfoProvider {
    suspend fun getAppName(packageName: String): String
    suspend fun getAppIcon(packageName: String): Any? // Platform specific icon
    suspend fun getInstalledApps(): List<AppMetadata>
    fun getAppVersionName(): String = "1.0.0"
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
