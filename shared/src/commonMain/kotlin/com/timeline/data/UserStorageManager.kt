package com.timeline.data

expect class UserStorageManager {
    fun getScreenshotDirectory(userId: String?): String
    fun copyGuestScreenshotsToUser(userId: String): Map<String, String>
    fun cleanupGuestScreenshots()
}
