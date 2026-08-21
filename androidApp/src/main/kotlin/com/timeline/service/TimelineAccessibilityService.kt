package com.timeline.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import co.touchlab.kermit.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TimelineAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Logger.d { "TimelineAccessibilityService connected" }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op, just need to be active
    }

    override fun onInterrupt() {
        Logger.d { "TimelineAccessibilityService interrupted" }
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    suspend fun captureScreenshot(): Bitmap? = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        val hardwareBuffer = screenshotResult.hardwareBuffer
                        val colorSpace = screenshotResult.colorSpace
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                        // Hardware bitmaps are not easily saved to file, so we might need to copy it
                        val softwareBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                        hardwareBuffer.close()
                        continuation.resume(softwareBitmap)
                    }

                    override fun onFailure(errorCode: Int) {
                        Logger.e { "takeScreenshot failed with error code: $errorCode" }
                        continuation.resume(null)
                    }
                })
            } catch (e: Exception) {
                Logger.e(e) { "Error calling takeScreenshot" }
                continuation.resume(null)
            }
        } else {
            continuation.resume(null)
        }
    }

    companion object {
        private var instance: TimelineAccessibilityService? = null
        
        fun getInstance(): TimelineAccessibilityService? = instance
    }
}
