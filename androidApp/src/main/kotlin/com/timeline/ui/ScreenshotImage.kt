package com.timeline.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.timeline.ui.theme.AppAlpha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

private object UiScreenshotCache {
    private const val THUMB_EDGE = 512
    private const val FULL_EDGE = 1600

    private fun bitmapCache(bytes: Int) = object : LruCache<String, ImageBitmap>(bytes) {
        override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
    }

    private val maxMem = Runtime.getRuntime().maxMemory()
    private val thumbs = bitmapCache((maxMem / 16).toInt())
    private val full = bitmapCache((maxMem / 8).toInt())

    // Few decodes at once so the main thread keeps its CPU
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(3))
    private val lock = Any()
    private val inFlight = HashMap<String, Deferred<ImageBitmap?>>()

    /** Instant, main-thread-safe lookup. For full-res, falls back to the thumb so the hero never starts blank. */
    fun peek(path: String, fullRes: Boolean): ImageBitmap? =
        if (fullRes) full.get(path) ?: thumbs.get(path) else thumbs.get(path) ?: full.get(path)

    suspend fun load(path: String, fullRes: Boolean): ImageBitmap? {
        val cache = if (fullRes) full else thumbs
        cache.get(path)?.let { return it }
        val key = "$path#$fullRes"
        val job = synchronized(lock) {
            inFlight[key] ?: scope.async {
                try {
                    decode(path, if (fullRes) FULL_EDGE else THUMB_EDGE, fullRes)
                        ?.also { cache.put(path, it) }
                } finally {
                    synchronized(lock) { inFlight.remove(key); Unit }
                }
            }.also { inFlight[key] = it }
        }
        return job.await()   // decode keeps running (and caching) even if the composable leaves
    }

    fun prefetch(paths: List<String>) {
        paths.forEach { p -> scope.launch { load(p, fullRes = false) } }
    }

    private fun decode(path: String, edge: Int, upTo: Boolean): ImageBitmap? {
        val file = File(path)
        if (!file.exists() || file.length() == 0L) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            val longEdge = maxOf(bounds.outWidth, bounds.outHeight)
            var sample = 1
            if (upTo) {
                while (longEdge / sample > edge) sample *= 2          // <= edge
            } else {
                while (longEdge / (sample * 2) >= edge) sample *= 2   // >= edge, stays sharp
            }
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
                ?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

fun prefetchScreenshots(paths: List<String>) = UiScreenshotCache.prefetch(paths)

@Composable
fun ScreenshotImage(
    path: String?,
    contentDescription: String? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fullResolution: Boolean = false
) {
    var bitmap by remember(path, fullResolution) {
        mutableStateOf(path?.let { UiScreenshotCache.peek(it, fullResolution) })
    }
    LaunchedEffect(path, fullResolution) {
        if (!path.isNullOrEmpty()) {
            UiScreenshotCache.load(path, fullResolution)?.let { bitmap = it }
        }
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AppAlpha.IconSecondary)
            )
        }
    }
}
