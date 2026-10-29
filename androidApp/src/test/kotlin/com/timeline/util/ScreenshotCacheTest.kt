package com.timeline.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScreenshotCacheTest {

    private fun createPixelBuffer(
        width: Int,
        height: Int,
        fillColor: Byte = 0x10,
        statusBarHeight: Int = 0,
        statusBarColor: Byte = 0x20
    ): ByteArray {
        val bytes = ByteArray(width * height * 4) { fillColor }
        if (statusBarHeight > 0) {
            val sbLimit = (statusBarHeight.coerceAtMost(height) * width * 4)
            for (i in 0 until sbLimit) {
                bytes[i] = statusBarColor
            }
        }
        return bytes
    }

    @Test
    fun testIdenticalBuffersProduceIdenticalHash() {
        val width = 100
        val height = 200
        val topPx = 24
        val buf1 = createPixelBuffer(width, height, fillColor = 0x33, statusBarHeight = topPx, statusBarColor = 0x11)
        val buf2 = createPixelBuffer(width, height, fillColor = 0x33, statusBarHeight = topPx, statusBarColor = 0x11)

        val hash1 = ScreenshotCache.computeBufferHash(buf1, width, height, topPx)
        val hash2 = ScreenshotCache.computeBufferHash(buf2, width, height, topPx)

        assertNotNull(hash1)
        assertNotNull(hash2)
        assertTrue(hash1.contentEquals(hash2), "Identical buffers must produce identical hashes")
    }

    @Test
    fun testDifferentBodyContentProducesDifferentHash() {
        val width = 100
        val height = 200
        val topPx = 24
        val buf1 = createPixelBuffer(width, height, fillColor = 0x33, statusBarHeight = topPx)
        val buf2 = createPixelBuffer(width, height, fillColor = 0x44, statusBarHeight = topPx)

        val hash1 = ScreenshotCache.computeBufferHash(buf1, width, height, topPx)
        val hash2 = ScreenshotCache.computeBufferHash(buf2, width, height, topPx)

        assertNotNull(hash1)
        assertNotNull(hash2)
        assertFalse(hash1.contentEquals(hash2), "Different body content must produce different hashes")
    }

    @Test
    fun testSinglePixelDifferenceInBodyDetected() {
        val width = 100
        val height = 200
        val topPx = 24
        val buf1 = createPixelBuffer(width, height, fillColor = 0x33, statusBarHeight = topPx)
        val buf2 = createPixelBuffer(width, height, fillColor = 0x33, statusBarHeight = topPx)

        // Mutate a single byte in row 50 (well inside the body region)
        val targetIndex = (50 * width + 10) * 4
        buf2[targetIndex] = (buf2[targetIndex] + 1).toByte()

        val hash1 = ScreenshotCache.computeBufferHash(buf1, width, height, topPx)
        val hash2 = ScreenshotCache.computeBufferHash(buf2, width, height, topPx)

        assertNotNull(hash1)
        assertNotNull(hash2)
        assertFalse(hash1.contentEquals(hash2), "Single pixel change in body must alter the hash")
    }

    @Test
    fun testStatusBarOnlyDifferenceIgnored() {
        val width = 100
        val height = 200
        val topPx = 30

        // buf1 and buf2 have identical body pixels (0x55)
        // buf1 has status bar color 0x11 (representing 12:30 clock, 90% battery)
        // buf2 has status bar color 0x22 (representing 12:31 clock, 89% battery)
        val buf1 = createPixelBuffer(width, height, fillColor = 0x55, statusBarHeight = topPx, statusBarColor = 0x11)
        val buf2 = createPixelBuffer(width, height, fillColor = 0x55, statusBarHeight = topPx, statusBarColor = 0x22)

        val hash1 = ScreenshotCache.computeBufferHash(buf1, width, height, topPx)
        val hash2 = ScreenshotCache.computeBufferHash(buf2, width, height, topPx)

        assertNotNull(hash1)
        assertNotNull(hash2)
        assertTrue(
            hash1.contentEquals(hash2),
            "Changes confined to the status-bar region must NOT alter the body-region hash"
        )
    }

    @Test
    fun testFirstCallNeverDuplicate() {
        ScreenshotCache.clear()
        val key = ScreenshotCache.getCacheKey("user1", "com.test.app")
        val hash = byteArrayOf(1, 2, 3, 4)

        val isDup = ScreenshotCache.updateAndCheckDuplicate(key, hash)
        assertFalse(isDup, "First call for a key must never report duplicate")
    }

    @Test
    fun testSubsequentIdenticalCallReportsDuplicate() {
        ScreenshotCache.clear()
        val key = ScreenshotCache.getCacheKey("user1", "com.test.app")
        val hash = byteArrayOf(1, 2, 3, 4)

        ScreenshotCache.updateAndCheckDuplicate(key, hash)
        val isDup = ScreenshotCache.updateAndCheckDuplicate(key, hash)
        assertTrue(isDup, "Second identical call must report duplicate")
    }

    @Test
    fun testSubsequentDifferentCallReportsNotDuplicate() {
        ScreenshotCache.clear()
        val key = ScreenshotCache.getCacheKey("user1", "com.test.app")
        val hash1 = byteArrayOf(1, 2, 3, 4)
        val hash2 = byteArrayOf(5, 6, 7, 8)

        ScreenshotCache.updateAndCheckDuplicate(key, hash1)
        val isDup = ScreenshotCache.updateAndCheckDuplicate(key, hash2)
        assertFalse(isDup, "Subsequent call with different hash must not report duplicate")
    }

    @Test
    fun testUserIsolationPreventsCrossUserSuppression() {
        ScreenshotCache.clear()
        val hash = byteArrayOf(10, 20, 30, 40)
        val guestKey = ScreenshotCache.getCacheKey(null, "com.youtube")
        val userKey = ScreenshotCache.getCacheKey("user_abc", "com.youtube")

        // Guest captures YouTube
        ScreenshotCache.updateAndCheckDuplicate(guestKey, hash)

        // Signed-in user opens YouTube with identical screen
        val isUserDup = ScreenshotCache.updateAndCheckDuplicate(userKey, hash)
        assertFalse(isUserDup, "New user session must not be suppressed by guest user cache")
    }

    @Test
    fun testClearResetsAllEntries() {
        ScreenshotCache.clear()
        val key = ScreenshotCache.getCacheKey("user1", "com.test.app")
        val hash = byteArrayOf(1, 2, 3, 4)

        ScreenshotCache.updateAndCheckDuplicate(key, hash)
        assertTrue(ScreenshotCache.updateAndCheckDuplicate(key, hash))

        ScreenshotCache.clear()
        assertFalse(
            ScreenshotCache.updateAndCheckDuplicate(key, hash),
            "After clear(), the baseline must be reset so the first call returns false"
        )
    }

    @Test
    fun testInvalidBoundsHandledSafely() {
        val width = 100
        val height = 200
        val buf = createPixelBuffer(width, height)

        // Status bar height exceeds or equals bitmap height
        val clampedHash = ScreenshotCache.computeBufferHash(buf, width, height, topPx = 250)
        assertNull(clampedHash, "When topPx exceeds height, null should be returned safely")

        // Zero width or height
        assertNull(ScreenshotCache.computeBufferHash(buf, 0, height, 10))
        assertNull(ScreenshotCache.computeBufferHash(buf, width, 0, 10))

        // Negative topPx clamped to 0
        val zeroCropHash = ScreenshotCache.computeBufferHash(buf, width, height, 0)
        val negCropHash = ScreenshotCache.computeBufferHash(buf, width, height, -10)
        assertNotNull(zeroCropHash)
        assertNotNull(negCropHash)
        assertTrue(zeroCropHash.contentEquals(negCropHash))
    }
}
