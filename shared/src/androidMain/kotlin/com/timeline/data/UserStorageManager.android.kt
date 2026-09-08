package com.timeline.data

import android.content.Context
import java.io.File
import java.util.UUID

actual class UserStorageManager(private val context: Context) {
    actual fun getScreenshotDirectory(userId: String?): String {
        val userFolder = userId ?: "guest"
        return File(context.filesDir, "screenshots/$userFolder").apply { mkdirs() }.absolutePath
    }

    actual fun copyGuestScreenshotsToUser(userId: String): Map<String, String> {
        val guestDir = File(context.filesDir, "screenshots/guest")
        if (!guestDir.exists() || !guestDir.isDirectory) return emptyMap()

        val userDir = File(context.filesDir, "screenshots/$userId").apply { mkdirs() }
        val pathMap = mutableMapOf<String, String>()

        val guestFiles = guestDir.listFiles() ?: return emptyMap()
        for (file in guestFiles) {
            if (!file.isFile) continue
            val originalName = file.name
            var targetFile = File(userDir, originalName)

            if (targetFile.exists()) {
                val nameWithoutExt = file.nameWithoutExtension
                val ext = file.extension
                val uuidSuffix = UUID.randomUUID().toString().take(8)
                val newName = "${nameWithoutExt}_$uuidSuffix.$ext"
                targetFile = File(userDir, newName)
            }

            try {
                file.copyTo(targetFile, overwrite = true)
                pathMap[file.absolutePath] = targetFile.absolutePath
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e(e) { "Failed to copy guest screenshot ${file.name}" }
            }
        }
        return pathMap
    }

    actual fun cleanupGuestScreenshots() {
        val guestDir = File(context.filesDir, "screenshots/guest")
        if (guestDir.exists() && guestDir.isDirectory) {
            guestDir.listFiles()?.forEach { it.delete() }
            guestDir.delete()
        }
    }
}
