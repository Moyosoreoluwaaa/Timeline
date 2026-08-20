package com.timeline.domain

interface AppInfoProvider {
    suspend fun getAppName(packageName: String): String
    suspend fun getAppIcon(packageName: String): Any? // Platform specific icon
}

data class AppMetadata(
    val packageName: String,
    val name: String,
    val icon: Any? = null
)
