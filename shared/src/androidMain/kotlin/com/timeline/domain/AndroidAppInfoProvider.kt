package com.timeline.domain

import android.content.Context
import android.content.pm.PackageManager

class AndroidAppInfoProvider(private val context: Context) : AppInfoProvider {
    private val packageManager: PackageManager = context.packageManager

    override suspend fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.split(".").last().replaceFirstChar { it.uppercase() }
        }
    }

    override suspend fun getAppIcon(packageName: String): Any? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
}
