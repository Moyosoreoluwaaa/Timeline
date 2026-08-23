package com.timeline.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
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
        
        // Optimize service configuration programmatically for lightweight screenshot capture only
        try {
            serviceInfo = serviceInfo?.apply {
                flags = AccessibilityServiceInfo.DEFAULT
                notificationTimeout = 100L
                eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to set AccessibilityServiceInfo programmatically" }
        }

        Logger.d { "TimelineAccessibilityService connected" }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op, just need to be active for screenshot capture
    }

    override fun onInterrupt() {
        Logger.d { "TimelineAccessibilityService interrupted" }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.d { "TimelineAccessibilityService unbound" }
        instance = null
        return super.onUnbind(intent)
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
                        // Hardware bitmaps are not easily saved to file, so we copy to software bitmap
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
        @Volatile
        private var instance: TimelineAccessibilityService? = null
        
        fun getInstance(): TimelineAccessibilityService? = instance
    }
}
