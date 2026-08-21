package com.timeline.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.SessionSegment
import com.timeline.domain.UserPreferences
import com.timeline.service.TimelineAccessibilityService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ScreenshotWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: TimelineRepository by inject()
    private val userPreferences: UserPreferences by inject()

    override suspend fun doWork(): Result {
        val packageName = inputData.getString("package_name") ?: return Result.failure()
        val sessionId = inputData.getString("session_id") ?: return Result.failure()
        
        val prefs = userPreferences.state.first()
        if (!prefs.isScreenshotCaptureEnabled) {
            Logger.d { "Screenshot capture disabled in settings, skipping for $packageName" }
            return Result.success()
        }

        Logger.d { "Taking screenshot for $packageName (Session: $sessionId)" }
        
        val accessibilityService = TimelineAccessibilityService.getInstance()
        val bitmap = if (accessibilityService != null) {
            accessibilityService.captureScreenshot()
        } else {
            Logger.w { "AccessibilityService not available (Service is likely not enabled in System Settings). Generating fallback snapshot for $packageName." }
            generateFallbackSnapshot(packageName)
        }

        if (bitmap != null) {
            val screenshotPath = saveBitmap(bitmap, packageName)
            if (screenshotPath != null) {
                updateSession(sessionId, screenshotPath)
            }
        }
        
        return Result.success()
    }

    private fun generateFallbackSnapshot(packageName: String): Bitmap {
        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        paint.color = 0xFF121212.toInt()
        canvas.drawRect(0f, 0f, 720f, 1280f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Snapshot of $packageName", 360f, 600f, paint)
        canvas.drawText(
            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date()),
            360f, 680f, paint
        )
        
        return bitmap
    }

    private fun saveBitmap(bitmap: Bitmap, packageName: String): String? {
        val filename = "screenshot_${packageName}_${System.currentTimeMillis()}.png"
        val file = File(applicationContext.filesDir, "screenshots").apply { mkdirs() }
        val targetFile = File(file, filename)
        
        return try {
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            Logger.e(e) { "Failed to save screenshot" }
            null
        }
    }

    private suspend fun updateSession(sessionId: String, screenshotPath: String) {
        val session = repository.getSession(sessionId)
        if (session != null) {
            val newSegment = SessionSegment(
                timestamp = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                screenshotPath = screenshotPath,
                activityDescription = "Snapshot captured"
            )
            val updatedSession = session.copy(
                screenshots = session.screenshots + screenshotPath,
                segments = session.segments + newSegment
            )
            repository.saveSession(updatedSession)
            Logger.d { "Updated session $sessionId with new screenshot" }
        }
    }
}
