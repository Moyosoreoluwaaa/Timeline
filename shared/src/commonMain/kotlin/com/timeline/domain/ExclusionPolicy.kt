package com.timeline.domain

interface ExclusionPolicy {
    fun isExcluded(packageName: String): Boolean
}

class TimelineExclusionPolicy : ExclusionPolicy {
    private val hardcodedExclusions = setOf(
        "com.timeline",
        "android",
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3"
    )

    override fun isExcluded(packageName: String): Boolean {
        return packageName in hardcodedExclusions || packageName.contains("launcher")
    }
}
