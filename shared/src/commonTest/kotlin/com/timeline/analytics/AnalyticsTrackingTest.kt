package com.timeline.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsTrackingTest {

    private class FakeAnalytics : Analytics {
        val loggedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val userProperties = mutableMapOf<String, String?>()
        var analyticsEnabled = true

        override fun log(event: String, params: Map<String, Any>) {
            if (!analyticsEnabled) return
            loggedEvents.add(event to params)
        }

        override fun setUserProperty(name: String, value: String?) {
            if (!analyticsEnabled) return
            userProperties[name] = value
        }

        override fun setEnabled(enabled: Boolean) {
            analyticsEnabled = enabled
        }
    }

    @Test
    fun testEventNamesFollowSnakeCaseAndMaxLength() {
        val allEvents = listOf(
            AnalyticsEvents.ONBOARDING_SCREEN_VIEW,
            AnalyticsEvents.ONBOARDING_COMPLETED,
            AnalyticsEvents.PERMISSION_PROMPT_SHOWN,
            AnalyticsEvents.PERMISSION_RESULT,
            AnalyticsEvents.TRACKING_STARTED,
            AnalyticsEvents.TUTORIAL_BEGIN,
            AnalyticsEvents.TUTORIAL_COMPLETE,
            AnalyticsEvents.TUTORIAL_SKIPPED,
            AnalyticsEvents.SCREEN_VIEW,
            AnalyticsEvents.SESSION_OPENED,
            AnalyticsEvents.HIGHLIGHTS_OPENED,
            AnalyticsEvents.HIGHLIGHTS_BATCH_DONE,
            AnalyticsEvents.HIGHLIGHTS_BATCH_FAILED,
            AnalyticsEvents.HIGHLIGHTS_NOTIFICATION_TAPPED,
            AnalyticsEvents.CAPTURE_TOGGLED,
            AnalyticsEvents.EXCLUSION_LIST_CHANGED,
            AnalyticsEvents.RETENTION_CHANGED,
            AnalyticsEvents.PAYWALL_VIEW,
            AnalyticsEvents.PAYWALL_DISMISSED,
            AnalyticsEvents.PURCHASE_STARTED,
            AnalyticsEvents.RESTORE_TAPPED,
            AnalyticsEvents.SERVICE_RESTARTED,
            AnalyticsEvents.ACCESSIBILITY_SERVICE_DISCONNECTED
        )

        val snakeCaseRegex = Regex("^[a-z][a-z0-9_]*$")

        allEvents.forEach { eventName ->
            assertTrue(
                eventName.length <= 40,
                "Event name '$eventName' exceeds 40 chars (${eventName.length})"
            )
            assertTrue(
                snakeCaseRegex.matches(eventName),
                "Event name '$eventName' does not match snake_case format"
            )
        }
    }

    @Test
    fun testParamNamesFollowSnakeCaseAndMaxLength() {
        val allParams = listOf(
            AnalyticsParams.STEP,
            AnalyticsParams.TYPE,
            AnalyticsParams.RESULT,
            AnalyticsParams.AT_STEP,
            AnalyticsParams.SCREEN_NAME,
            AnalyticsParams.SESSIONS_BUCKET,
            AnalyticsParams.SOURCE,
            AnalyticsParams.WINDOW,
            AnalyticsParams.ENGINE,
            AnalyticsParams.LATENCY_BUCKET,
            AnalyticsParams.REASON,
            AnalyticsParams.ENABLED,
            AnalyticsParams.EXCLUDED_COUNT,
            AnalyticsParams.DAYS
        )

        val snakeCaseRegex = Regex("^[a-z][a-z0-9_]*$")

        allParams.forEach { paramName ->
            assertTrue(
                paramName.length <= 40,
                "Param name '$paramName' exceeds 40 chars (${paramName.length})"
            )
            assertTrue(
                snakeCaseRegex.matches(paramName),
                "Param name '$paramName' does not match snake_case format"
            )
        }
    }

    @Test
    fun testNoForbiddenKeysInDefinedParams() {
        val forbiddenSubstrings = listOf("ocr", "screenshot", "window_title", "package_name", "package")
        val allParams = listOf(
            AnalyticsParams.STEP,
            AnalyticsParams.TYPE,
            AnalyticsParams.RESULT,
            AnalyticsParams.AT_STEP,
            AnalyticsParams.SCREEN_NAME,
            AnalyticsParams.SESSIONS_BUCKET,
            AnalyticsParams.SOURCE,
            AnalyticsParams.WINDOW,
            AnalyticsParams.ENGINE,
            AnalyticsParams.LATENCY_BUCKET,
            AnalyticsParams.REASON,
            AnalyticsParams.ENABLED,
            AnalyticsParams.EXCLUDED_COUNT,
            AnalyticsParams.DAYS
        )

        allParams.forEach { paramName ->
            forbiddenSubstrings.forEach { forbidden ->
                assertFalse(
                    paramName.contains(forbidden),
                    "Param '$paramName' contains forbidden sensitive key substring '$forbidden'"
                )
            }
        }
    }

    @Test
    fun testNoOpAnalyticsDoesNotThrow() {
        val noOp = NoOpAnalytics()
        noOp.log("test_event", mapOf("key" to "value"))
        noOp.setUserProperty("is_pro", "true")
        noOp.setEnabled(false)
        noOp.setEnabled(true)
    }

    @Test
    fun testConsentOffDisablesLogging() {
        val analytics = FakeAnalytics()
        analytics.log("first_event", mapOf("key" to "val1"))
        assertEquals(1, analytics.loggedEvents.size)

        analytics.setEnabled(false)
        analytics.log("second_event", mapOf("key" to "val2"))
        analytics.setUserProperty("is_pro", "true")
        assertEquals(1, analytics.loggedEvents.size)
        assertTrue(analytics.userProperties.isEmpty())

        analytics.setEnabled(true)
        analytics.log("third_event", mapOf("key" to "val3"))
        assertEquals(2, analytics.loggedEvents.size)
    }

    @Test
    fun testSessionsBucketLogic() {
        fun bucket(count: Int): String = when {
            count == 0 -> "0"
            count in 1..10 -> "1-10"
            else -> "10+"
        }

        assertEquals("0", bucket(0))
        assertEquals("1-10", bucket(1))
        assertEquals("1-10", bucket(5))
        assertEquals("1-10", bucket(10))
        assertEquals("10+", bucket(11))
        assertEquals("10+", bucket(100))
    }

    @Test
    fun testLatencyBucketLogic() {
        fun latencyBucket(ms: Long): String = when {
            ms < 5_000 -> "<5s"
            ms < 15_000 -> "5-15s"
            ms < 30_000 -> "15-30s"
            else -> "30s+"
        }

        assertEquals("<5s", latencyBucket(100))
        assertEquals("<5s", latencyBucket(4999))
        assertEquals("5-15s", latencyBucket(5000))
        assertEquals("5-15s", latencyBucket(14999))
        assertEquals("15-30s", latencyBucket(15000))
        assertEquals("15-30s", latencyBucket(29999))
        assertEquals("30s+", latencyBucket(30000))
        assertEquals("30s+", latencyBucket(65000))
    }
}
