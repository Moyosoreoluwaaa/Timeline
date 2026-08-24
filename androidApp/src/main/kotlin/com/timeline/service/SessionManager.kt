package com.timeline.service

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import co.touchlab.kermit.Logger
import com.timeline.data.TimelineRepository
import com.timeline.domain.Session
import com.timeline.worker.ScreenshotWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class SessionManager(
    private val context: Context,
    private val repository: TimelineRepository,
    private val serviceScope: CoroutineScope
) {
    var currentSessionId: String? = null
        private set

    fun startNewSession(packageName: String): String {
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId
        
        serviceScope.launch {
            try {
                val session = Session(
                    id = sessionId,
                    packageName = packageName,
                    startTime = kotlin.time.Clock.System.now(),
                    endTime = null,
                    durationMinutes = 0,
                    screenshots = emptyList(),
                    segments = emptyList()
                )
                repository.saveSession(session)
                Logger.i(tag = "SessionManager") { "Successfully saved session for $packageName" }
                enqueueScreenshot(sessionId, packageName)
            } catch (e: Exception) {
                Logger.e(e, "SessionManager") { "Failed to start session for $packageName" }
            }
        }
        return sessionId
    }

    fun closePreviousSession(sessionId: String) {
        serviceScope.launch {
            try {
                val session = repository.getSession(sessionId)
                if (session != null) {
                    val endTime = kotlin.time.Clock.System.now()
                    val duration = (endTime - session.startTime).inWholeMinutes
                    val updatedSession = session.copy(
                        endTime = endTime,
                        durationMinutes = duration
                    )
                    repository.saveSession(updatedSession)
                    Logger.i(tag = "SessionManager") { "Closed session $sessionId. Duration: ${duration}m" }
                }
            } catch (e: Exception) {
                Logger.e(e, "SessionManager") { "Failed to close session $sessionId" }
            }
        }
    }

    fun clearCurrentSession() {
        currentSessionId = null
    }

    fun enqueueScreenshot(sessionId: String, packageName: String) {
        val workRequest = OneTimeWorkRequestBuilder<ScreenshotWorker>()
            .setInputData(workDataOf(
                "package_name" to packageName,
                "session_id" to sessionId
            ))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
        Logger.d(tag = "SessionManager") { "Enqueued ScreenshotWorker for $packageName (Session: $sessionId)" }
    }
}
