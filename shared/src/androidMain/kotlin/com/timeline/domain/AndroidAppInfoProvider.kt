package com.timeline.domain

import android.content.Context
import android.content.pm.PackageManager
import co.touchlab.kermit.Logger

class AndroidAppInfoProvider(private val context: Context) : AppInfoProvider {
    private val packageManager: PackageManager = context.packageManager
    private val nameCache = mutableMapOf<String, String>()
    private val iconCache = mutableMapOf<String, Any?>()

    override suspend fun getAppName(packageName: String): String {
        nameCache[packageName]?.let { return it }
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString()
            Logger.d(tag = "AndroidAppInfoProvider") { "Resolved label for $packageName: $label" }
            nameCache[packageName] = label
            label
        } catch (e: Exception) {
            val fallback = packageName.split(".").last().replaceFirstChar { it.uppercase() }
            Logger.w(tag = "AndroidAppInfoProvider") { "Failed to get label for $packageName, using fallback: $fallback. Error: ${e.message}" }
            fallback
        }
    }

    override suspend fun getAppIcon(packageName: String): Any? {
        iconCache[packageName]?.let { return it }
        return try {
            val icon = packageManager.getApplicationIcon(packageName)
            Logger.d(tag = "AndroidAppInfoProvider") { "Resolved icon for $packageName" }
            iconCache[packageName] = icon
            icon
        } catch (e: Exception) {
            Logger.w(tag = "AndroidAppInfoProvider") { "Failed to get icon for $packageName. Error: ${e.message}" }
            null
        }
    }
}
