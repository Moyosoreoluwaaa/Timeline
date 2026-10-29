package com.timeline.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Detects exact-duplicate screenshots before they are written to disk.
 *
 * ## How it works
 * A SHA-256 digest is computed over the raw ARGB_8888 pixel bytes of the *body* region of the
 * bitmap — the portion below the status bar. The status-bar strip is excluded because its clock,
 * battery, and notification icons change independently of application content.
 *
 * ## Status-bar height
 * The height is read once from the Android system resource "status_bar_height". If that resource
 * is unavailable (custom ROMs), or if the height is zero (full-screen / immersive apps), or if it
 * equals or exceeds the bitmap height (degenerate capture), the entire bitmap is hashed so that no
 * application content is accidentally excluded.
 *
 * ## Cache key
 * Entries are keyed by "$userId:$packageName" so that cache state from one account can never
 * suppress a first capture in a different account's session.
 *
 * ## Lifecycle
 * Call [clear] on sign-out to reset all stored hashes.
 */
object ScreenshotCache {

    // Maps "$userId:$packageName" → last SHA-256 body-region digest.
    private val lastHashes = ConcurrentHashMap<String, ByteArray>()

    // Cached status-bar height in pixels. -1 = not yet read.
    @Volatile private var cachedStatusBarPx: Int = UNSET

    private const val UNSET = -1

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns `true` if [bitmap] is pixel-identical (in the body region) to the previous capture
     * for the same [userId]/[packageName] pair.
     *
     * The first call for any given key always returns `false` (no prior baseline to compare).
     *
     * The [bitmap] must be in [Bitmap.Config.ARGB_8888]; bitmaps in HARDWARE config are not
     * supported and will return `false` without storing a hash.
     */
    fun isDuplicate(
        context: Context,
        userId: String?,
        packageName: String,
        bitmap: Bitmap,
    ): Boolean {
        if (bitmap.config != Bitmap.Config.ARGB_8888) return false

        val key = cacheKey(userId, packageName)
        val sbHeight = resolvedStatusBarPx(context, bitmap.height)
        val hash = computeBodyHash(bitmap, sbHeight) ?: return false

        return updateAndCheckDuplicate(key, hash)
    }

    /**
     * Checks if [hash] matches the stored hash for [key], and updates the cache with [hash].
     * Returns true only if a previous hash existed and was byte-for-byte identical.
     */
    internal fun updateAndCheckDuplicate(key: String, hash: ByteArray): Boolean {
        val lastHash = lastHashes[key]
        lastHashes[key] = hash
        return lastHash != null && lastHash.contentEquals(hash)
    }

    internal fun getCacheKey(userId: String?, packageName: String): String =
        cacheKey(userId, packageName)

    /**
     * Clears all stored hashes and the cached status-bar height.
     * Must be called on sign-out to prevent cross-account hash comparisons.
     */
    fun clear() {
        lastHashes.clear()
        cachedStatusBarPx = UNSET
    }

    // ── Internal helpers (internal visibility for white-box unit tests) ────────

    /**
     * Computes a SHA-256 digest over the ARGB_8888 pixel bytes of [bitmap] starting at row
     * [bodyTopPx]. If [bodyTopPx] is 0 (no exclusion) the full bitmap is hashed.
     *
     * Returns `null` only if the body region is empty after clamping (degenerate bitmap).
     */
    internal fun computeBodyHash(bitmap: Bitmap, bodyTopPx: Int): ByteArray? {
        val top = bodyTopPx.coerceIn(0, bitmap.height)
        val bodyHeight = bitmap.height - top
        if (bodyHeight <= 0) return null

        val buf = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buf)
        return computeBufferHash(buf.array(), bitmap.width, bitmap.height, top)
    }

    /**
     * Computes a SHA-256 digest over raw ARGB_8888 pixel bytes starting from row [topPx].
     * Pure JVM-safe function testable without Android framework classes.
     */
    internal fun computeBufferHash(
        bytes: ByteArray,
        width: Int,
        height: Int,
        topPx: Int
    ): ByteArray? {
        if (width <= 0 || height <= 0) return null
        val top = topPx.coerceIn(0, height)
        val bodyHeight = height - top
        if (bodyHeight <= 0) return null

        val bytesPerPixel = 4
        val rowBytes = width * bytesPerPixel
        val offset = top * rowBytes
        val length = bodyHeight * rowBytes
        if (offset + length > bytes.size) return null

        val md = MessageDigest.getInstance("SHA-256")
        md.update(bytes, offset, length)
        return md.digest()
    }

    /**
     * Returns the status-bar height in pixels, clamped so it is always a safe crop offset.
     *
     * A value of 0 is returned when:
     * - The system resource is unavailable (custom ROM).
     * - The resource reports 0 (app is in immersive/full-screen mode).
     * - The height equals or exceeds [bitmapHeight] (degenerate capture).
     */
    internal fun resolvedStatusBarPx(context: Context, bitmapHeight: Int): Int {
        val raw = getOrFetchStatusBarPx(context)
        // Never crop more than (height - 1) — always keep at least 1 pixel of body.
        return raw.coerceIn(0, maxOf(0, bitmapHeight - 1))
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun cacheKey(userId: String?, packageName: String): String =
        "${userId ?: "guest"}:$packageName"

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getOrFetchStatusBarPx(context: Context): Int {
        val cached = cachedStatusBarPx
        if (cached != UNSET) return cached

        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val px = if (resId > 0) context.resources.getDimensionPixelSize(resId) else 0
        cachedStatusBarPx = px
        return px
    }
}
