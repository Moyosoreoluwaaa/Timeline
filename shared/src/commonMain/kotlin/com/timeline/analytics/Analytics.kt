package com.timeline.analytics

interface Analytics {
    fun log(event: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String?)
    fun setEnabled(enabled: Boolean)
}
