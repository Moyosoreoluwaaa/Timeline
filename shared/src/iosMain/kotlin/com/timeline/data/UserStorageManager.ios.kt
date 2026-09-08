package com.timeline.data

import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSFileManager

import kotlinx.cinterop.ExperimentalForeignApi

actual class UserStorageManager {
    @OptIn(ExperimentalForeignApi::class)
    actual fun getScreenshotDirectory(userId: String?): String {
        val userFolder = userId ?: "guest"
        val path = NSHomeDirectory() + "/Documents/screenshots/$userFolder"
        NSFileManager.defaultManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
        return path
    }

    actual fun copyGuestScreenshotsToUser(userId: String): Map<String, String> {
        return emptyMap()
    }

    actual fun cleanupGuestScreenshots() {
    }
}
