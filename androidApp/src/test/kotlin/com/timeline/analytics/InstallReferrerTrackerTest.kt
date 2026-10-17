package com.timeline.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallReferrerTrackerTest {

    @Test
    fun testParseInstallSourceInstagram() {
        val url = "utm_source=instagram&utm_medium=cpc&utm_campaign=launch"
        assertEquals("instagram", InstallReferrerTracker.parseInstallSource(url))
    }

    @Test
    fun testParseInstallSourceTikTok() {
        val url = "https://play.google.com/store/apps/details?id=com.timeline_records&utm_source=tiktok&utm_medium=bio"
        assertEquals("tiktok", InstallReferrerTracker.parseInstallSource(url))
    }

    @Test
    fun testParseInstallSourceYouTube() {
        val url = "utm_source=youtube&utm_campaign=review"
        assertEquals("youtube", InstallReferrerTracker.parseInstallSource(url))
    }

    @Test
    fun testParseInstallSourceReddit() {
        val url = "utm_source=reddit&utm_content=post"
        assertEquals("reddit", InstallReferrerTracker.parseInstallSource(url))
    }

    @Test
    fun testParseInstallSourceTwitterAndX() {
        assertEquals("x", InstallReferrerTracker.parseInstallSource("utm_source=twitter"))
        assertEquals("x", InstallReferrerTracker.parseInstallSource("utm_source=x"))
    }

    @Test
    fun testParseInstallSourceOrganicFallback() {
        assertEquals("organic", InstallReferrerTracker.parseInstallSource(null))
        assertEquals("organic", InstallReferrerTracker.parseInstallSource(""))
        assertEquals("organic", InstallReferrerTracker.parseInstallSource("utm_medium=direct"))
    }
}
