package com.timeline.domain

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import java.util.Calendar

class AndroidDeviceUsageSyncer(
    private val context: Context,
    private val repository: TimelineRepository,
    private val exclusionPolicy: ExclusionPolicy,
    private val logger: Logger
) : DeviceUsageSyncer {

    private val tagLogger = logger.withTag("AndroidDeviceUsageSyncer")
    private val packageManager: PackageManager = context.packageManager

    override fun hasPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            if (mode == AppOpsManager.MODE_ALLOWED) {
                return true
            }
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usageStatsManager?.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60 * 60, now)
            stats != null && stats.isNotEmpty()
        } catch (e: Exception) {
            tagLogger.e(e) { "Error checking usage stats permission" }
            false
        }
    }

    override suspend fun syncRealDeviceUsage(daysBack: Int): Int = withContext(Dispatchers.IO) {
        val permissionGranted = hasPermission()
        var syncedCount = 0

        if (permissionGranted) {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                if (usageStatsManager != null) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -daysBack.coerceAtLeast(1))
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startTime = calendar.timeInMillis
                    val endTime = System.currentTimeMillis()

                    tagLogger.d { "Syncing real usage from $startTime to $endTime" }

                    // 1. First attempt: Query fine-grained UsageEvents
                    val events = usageStatsManager.queryEvents(startTime, endTime)
                    val event = UsageEvents.Event()
                    
                    var currentPackage: String? = null
                    var currentSessionStart = 0L

                    while (events.hasNextEvent()) {
                        events.getNextEvent(event)
                        val pkg = event.packageName ?: continue

                        if (exclusionPolicy.isExcluded(pkg) || pkg == context.packageName) {
                            continue
                        }

                        when (event.eventType) {
                            UsageEvents.Event.ACTIVITY_RESUMED -> {
                                if (currentPackage != null && currentPackage != pkg && currentSessionStart > 0L) {
                                    val durationMs = event.timeStamp - currentSessionStart
                                    if (durationMs >= 5000L) { // At least 5 seconds
                                        val saved = saveDeviceSession(currentPackage, currentSessionStart, event.timeStamp)
                                        if (saved) syncedCount++
                                    }
                                }
                                currentPackage = pkg
                                currentSessionStart = event.timeStamp
                            }
                            UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                                if (currentPackage == pkg && currentSessionStart > 0L) {
                                    val durationMs = event.timeStamp - currentSessionStart
                                    if (durationMs >= 5000L) {
                                        val saved = saveDeviceSession(pkg, currentSessionStart, event.timeStamp)
                                        if (saved) syncedCount++
                                    }
                                    currentPackage = null
                                    currentSessionStart = 0L
                                }
                            }
                        }
                    }

                    // Close trailing active session if still open
                    if (currentPackage != null && currentSessionStart > 0L) {
                        val durationMs = endTime - currentSessionStart
                        if (durationMs >= 5000L) {
                            val saved = saveDeviceSession(currentPackage, currentSessionStart, endTime)
                            if (saved) syncedCount++
                        }
                    }

                    // 2. Second attempt: If queryEvents was sparse, supplement with aggregate UsageStats
                    if (syncedCount < 3) {
                        tagLogger.d { "QueryEvents produced $syncedCount sessions. Supplementing with queryUsageStats..." }
                        val statsList = usageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            startTime,
                            endTime
                        ) ?: emptyList()

                        for (stats in statsList) {
                            val pkg = stats.packageName ?: continue
                            if (exclusionPolicy.isExcluded(pkg) || pkg == context.packageName) continue
                            if (stats.totalTimeInForeground < 5000L) continue // At least 5 seconds

                            val sTime = stats.firstTimeStamp.coerceAtLeast(startTime)
                            val eTime = stats.lastTimeUsed.coerceAtLeast(sTime + stats.totalTimeInForeground)

                            val saved = saveDeviceSession(
                                packageName = pkg,
                                startTimeMs = sTime,
                                endTimeMs = eTime,
                                durationMs = stats.totalTimeInForeground
                            )
                            if (saved) syncedCount++
                        }
                    }
                }
            } catch (e: Exception) {
                tagLogger.e { "Failed to query UsageStatsManager: ${e.message}" }
            }
        } else {
            tagLogger.w { "Usage stats permission not granted." }
        }

        tagLogger.i { "Finished real usage sync. Successfully saved $syncedCount sessions." }
        syncedCount
    }

    private suspend fun saveDeviceSession(
        packageName: String,
        startTimeMs: Long,
        endTimeMs: Long,
        durationMs: Long = (endTimeMs - startTimeMs).coerceAtLeast(1000L)
    ): Boolean {
        val sessionId = "real_${packageName}_${startTimeMs / 1000}"
        
        // Don't overwrite if already recorded
        val existing = repository.getSession(sessionId)
        if (existing != null) {
            return false
        }

        val appName = try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.split(".").last().replaceFirstChar { it.uppercase() }
        }

        val appIcon = try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }

        val durationMinutes = (durationMs / 60000L).coerceAtLeast(1L)

        val session = Session(
            id = sessionId,
            packageName = packageName,
            displayName = appName,
            icon = appIcon,
            startTime = Instant.fromEpochMilliseconds(startTimeMs),
            endTime = Instant.fromEpochMilliseconds(endTimeMs),
            durationMinutes = durationMinutes,
            screenshots = emptyList(),
            segments = emptyList()
        )

        repository.saveSession(session)
        tagLogger.d { "Saved real session for $appName ($packageName), duration: ${durationMinutes}m" }
        return true
    }
}
