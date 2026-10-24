package com.timeline.domain

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import java.util.Calendar
import kotlin.time.Instant

/**
 * Syncs real device usage sessions into the Timeline repository.
 * Applies early returns and declarative filtering pipelines to avoid deep loop nesting.
 */
class AndroidDeviceUsageSyncer(
    private val context: Context,
    private val repository: TimelineRepository,
    private val exclusionPolicy: ExclusionPolicy,
    logger: Logger
) : DeviceUsageSyncer {

    private val packageManager: PackageManager = context.packageManager
    private val tagLogger = logger.withTag("AndroidDeviceUsageSyncer")

    override fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
            ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    override suspend fun syncRealDeviceUsage(daysBack: Int): Int {
        if (!hasPermission()) {
            tagLogger.w { "Usage stats permission not granted." }
            return 0
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        if (usageStatsManager == null) {
            tagLogger.e { "UsageStatsManager service is unavailable." }
            return 0
        }

        val (startTime, endTime) = calculateSyncWindow(daysBack)
        tagLogger.d { "Syncing real usage from $startTime to $endTime" }

        var syncedCount = 0
        try {
            // 1. Primary: Fine-grained UsageEvents
            val events = usageStatsManager.queryEvents(startTime, endTime)
            syncedCount += processUsageEvents(events, endTime)

            // 2. Secondary: Fallback to aggregate UsageStats if fine-grained events are sparse
            if (syncedCount < 3) {
                tagLogger.d { "UsageEvents produced only $syncedCount sessions. Supplementing with queryUsageStats..." }
                val statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime) ?: emptyList()
                syncedCount += processAggregateStats(statsList, startTime)
            }
        } catch (e: Exception) {
            tagLogger.e { "Failed to query UsageStatsManager: ${e.message}" }
        }

        tagLogger.i { "Finished real usage sync. Successfully saved $syncedCount sessions." }
        return syncedCount
    }

    private fun calculateSyncWindow(daysBack: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysBack.coerceAtLeast(1))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Pair(calendar.timeInMillis, System.currentTimeMillis())
    }

    private suspend fun processUsageEvents(events: UsageEvents, endTime: Long): Int {
        var count = 0
        val event = UsageEvents.Event()
        var currentPackage: String? = null
        var currentSessionStart = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (isPackageIgnored(pkg)) continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (currentPackage != null && currentPackage != pkg && currentSessionStart > 0L) {
                        if (saveIfValidDuration(currentPackage, currentSessionStart, event.timeStamp)) count++
                    }
                    currentPackage = pkg
                    currentSessionStart = event.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (currentPackage == pkg && currentSessionStart > 0L) {
                        if (saveIfValidDuration(pkg, currentSessionStart, event.timeStamp)) count++
                        currentPackage = null
                        currentSessionStart = 0L
                    }
                }
            }
        }

        if (currentPackage != null && currentSessionStart > 0L) {
            if (saveIfValidDuration(currentPackage, currentSessionStart, endTime)) count++
        }
        return count
    }

    private suspend fun processAggregateStats(statsList: List<UsageStats>, startTime: Long): Int {
        var count = 0
        for (stats in statsList) {
            val pkg = stats.packageName ?: continue
            if (isPackageIgnored(pkg)) continue
            if (stats.totalTimeInForeground < 5000L) continue

            val sTime = stats.firstTimeStamp.coerceAtLeast(startTime)
            val eTime = stats.lastTimeUsed.coerceAtLeast(sTime + stats.totalTimeInForeground)
            val saved = saveDeviceSession(pkg, sTime, eTime, stats.totalTimeInForeground)
            if (saved) count++
        }
        return count
    }

    private suspend fun isPackageIgnored(pkg: String): Boolean =
        pkg == context.packageName || exclusionPolicy.isExcluded(pkg)

    private suspend fun saveIfValidDuration(packageName: String, startMs: Long, endMs: Long): Boolean {
        val durationMs = endMs - startMs
        if (durationMs < 5000L) return false
        return saveDeviceSession(packageName, startMs, endMs, durationMs)
    }

    private suspend fun saveDeviceSession(
        packageName: String,
        startTimeMs: Long,
        endTimeMs: Long,
        durationMs: Long = (endTimeMs - startTimeMs).coerceAtLeast(1000L)
    ): Boolean {
        val sessionId = "real_${packageName}_${startTimeMs / 1000}"
        if (repository.getSession(sessionId) != null) return false

        val appName = try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast(".")
        }

        val session = Session(
            id = sessionId,
            packageName = packageName,
            displayName = appName,
            startTime = Instant.fromEpochMilliseconds(startTimeMs),
            endTime = Instant.fromEpochMilliseconds(endTimeMs),
            durationMinutes = (durationMs / 60000L).coerceAtLeast(0L),
            durationSeconds = (durationMs / 1000L).coerceAtLeast(1L),
            screenshots = emptyList(),
            segments = emptyList()
        )
        repository.saveSession(session)
        return true
    }
}
