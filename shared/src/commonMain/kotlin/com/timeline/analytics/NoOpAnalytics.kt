package com.timeline.analytics

class NoOpAnalytics : Analytics {
    override fun log(event: String, params: Map<String, Any>) {}
    override fun setUserProperty(name: String, value: String?) {}
    override fun setEnabled(enabled: Boolean) {}
}
