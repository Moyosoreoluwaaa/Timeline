package com.timeline.analytics

object AnalyticsEvents {
    // 1. Onboarding and permissions
    const val ONBOARDING_SCREEN_VIEW = "onboarding_screen_view"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val PERMISSION_PROMPT_SHOWN = "permission_prompt_shown"
    const val PERMISSION_RESULT = "permission_result"
    const val TRACKING_STARTED = "tracking_started"

    // 2. Tutorial
    const val TUTORIAL_BEGIN = "tutorial_begin"
    const val TUTORIAL_COMPLETE = "tutorial_complete"
    const val TUTORIAL_SKIPPED = "tutorial_skipped"

    // 3. Core usage
    const val SCREEN_VIEW = "screen_view"
    const val SESSION_OPENED = "session_opened"
    const val HIGHLIGHTS_OPENED = "highlights_opened"

    // 4. Highlights pipeline
    const val HIGHLIGHTS_BATCH_DONE = "highlights_batch_done"
    const val HIGHLIGHTS_BATCH_FAILED = "highlights_batch_failed"
    const val HIGHLIGHTS_NOTIFICATION_TAPPED = "highlights_notification_tapped"

    // 5. Settings and control
    const val CAPTURE_TOGGLED = "capture_toggled"
    const val EXCLUSION_LIST_CHANGED = "exclusion_list_changed"
    const val RETENTION_CHANGED = "retention_changed"

    // 6. Monetization
    const val PAYWALL_VIEW = "paywall_view"
    const val PAYWALL_DISMISSED = "paywall_dismissed"
    const val PURCHASE_STARTED = "purchase_started"
    const val RESTORE_TAPPED = "restore_tapped"

    // 7. Reliability
    const val SERVICE_RESTARTED = "service_restarted"
    const val ACCESSIBILITY_SERVICE_DISCONNECTED = "accessibility_service_disconnected"
}

object AnalyticsParams {
    const val STEP = "step"
    const val TYPE = "type"
    const val RESULT = "result"
    const val AT_STEP = "at_step"
    const val SCREEN_NAME = "screen_name"
    const val SESSIONS_BUCKET = "sessions_bucket"
    const val SOURCE = "source"
    const val WINDOW = "window"
    const val ENGINE = "engine"
    const val LATENCY_BUCKET = "latency_bucket"
    const val REASON = "reason"
    const val ENABLED = "enabled"
    const val EXCLUDED_COUNT = "excluded_count"
    const val DAYS = "days"
}

object AnalyticsUserProperties {
    const val IS_PRO = "is_pro"
    const val DEVICE_BRAND = "device_brand"
    const val INSTALL_SOURCE = "install_source"
    const val AI_REASONING_ENABLED = "ai_reasoning_enabled"
}
