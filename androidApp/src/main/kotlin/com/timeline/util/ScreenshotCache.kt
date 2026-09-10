package com.timeline.util

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentHashMap

object ScreenshotCache {
    private val lastHashes = ConcurrentHashMap<String, Int>()

    fun isDuplicate(packageName: String, bitmap: Bitmap): Boolean {
        // Simple hash based on a downscaled bitmap
        val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, false)
        val hash = scaled.hashCode()
        scaled.recycle()
        
        val lastHash = lastHashes[packageName]
        lastHashes[packageName] = hash
        
        return lastHash == hash
    }
}
