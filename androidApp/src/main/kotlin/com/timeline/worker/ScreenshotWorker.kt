package com.timeline.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.SessionSegment
import com.timeline.domain.UserPreferences
import com.timeline.service.TimelineAccessibilityService
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.time.Instant

class ScreenshotWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: TimelineRepository by inject()
    private val userPreferences: UserPreferences by inject()
    private val userStorageManager: com.timeline.data.UserStorageManager by inject()

    override suspend fun doWork(): Result {
        val packageName = inputData.getString("package_name") ?: return Result.failure()
        val sessionId = inputData.getString("session_id") ?: return Result.failure()
        
        val prefs = userPreferences.state.first()
        if (!prefs.isScreenshotCaptureEnabled) {
            Logger.d { "Screenshot capture disabled in settings, skipping for $packageName" }
            return Result.success()
        }

        val session = repository.getSession(sessionId)
        if (session == null) {
            Logger.w { "Session $sessionId not found when capturing screenshot, skipping" }
            return Result.success()
        }

        Logger.d { "Taking screenshot for $packageName (Session: $sessionId, User: ${session.userId})" }
        
        val accessibilityService = TimelineAccessibilityService.getInstance()
        if (accessibilityService == null) {
            Logger.w { "AccessibilityService not available (Service is likely not enabled in System Settings). Cannot capture real screenshot for $packageName." }
            return Result.success()
        }

        val bitmap = accessibilityService.captureScreenshot()
        if (bitmap != null) {
            val screenshotPath = saveBitmap(bitmap, packageName, session.userId)
            if (screenshotPath != null) {
                updateSession(session, screenshotPath)
            }
        }
        
        return Result.success()
    }

    private fun saveBitmap(bitmap: android.graphics.Bitmap, packageName: String, userId: String?): String? {
        val storageDir = File(userStorageManager.getScreenshotDirectory(userId))
        val filename = "screenshot_${packageName}_${System.currentTimeMillis()}.png"
        val targetFile = File(storageDir, filename)
        val tempFile = File(storageDir, ".temp_${UUID.randomUUID()}.png")
        
        return try {
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, out)
                out.flush()
            }
            if (tempFile.renameTo(targetFile)) {
                targetFile.absolutePath
            } else {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
                targetFile.absolutePath
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to save screenshot" }
            if (tempFile.exists()) tempFile.delete()
            null
        }
    }

    private suspend fun updateSession(session: com.timeline.domain.Session, screenshotPath: String) {
        val newSegment = SessionSegment(
            timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            screenshotPath = screenshotPath,
            activityDescription = "Snapshot captured"
        )
        val updatedSession = session.copy(
            screenshots = session.screenshots + screenshotPath,
            segments = session.segments + newSegment
        )
        repository.saveSession(updatedSession)
        Logger.d { "Updated session ${session.id} with new screenshot" }
    }
}
