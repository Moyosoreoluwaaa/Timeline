package com.timeline.tutorial

enum class TutorialScreen {
    TIMELINE,
    HIGHLIGHT,
    SETTINGS
}

enum class TutorialStep(
    val screen: TutorialScreen,
    val title: String,
    val description: String
) {
    PREREQUISITE_CHECK(
        screen = TutorialScreen.TIMELINE,
        title = "Preparing Showcase",
        description = "Verifying mock usage data and screenshots..."
    ),
    SPOTLIGHT_APP_ENTRY(
        screen = TutorialScreen.TIMELINE,
        title = "App Usage Entry",
        description = "Tap an app entry to inspect recorded usage sessions."
    ),
    SPOTLIGHT_SCREENSHOT_THUMBNAIL(
        screen = TutorialScreen.TIMELINE,
        title = "Screenshot Thumbnail",
        description = "Tap the screenshot thumbnail to view full-screen captures."
    ),
    FULL_SCREEN_IMAGE_PREVIEW(
        screen = TutorialScreen.TIMELINE,
        title = "Full-Screen Preview",
        description = "Tap anywhere on the overlay to dismiss the preview."
    ),
    EXPAND_BOTTOM_SHEET(
        screen = TutorialScreen.TIMELINE,
        title = "Expand Sheet",
        description = "Drag or expand the bottom sheet to view detailed sessions."
    ),
    SPOTLIGHT_SESSION_NAVIGATOR(
        screen = TutorialScreen.TIMELINE,
        title = "Session Controls",
        description = "Use Previous and Next controls to cycle between sessions."
    ),
    SPOTLIGHT_TIME_FILTER_ICON(
        screen = TutorialScreen.TIMELINE,
        title = "Time Filter Icon",
        description = "Tap the clock icon to reveal filter options by time of day."
    ),
    SPOTLIGHT_TIME_FILTER_SECTION(
        screen = TutorialScreen.TIMELINE,
        title = "Time Filter Options",
        description = "Filter your timeline sessions by Morning, Afternoon, or Evening."
    ),
    SPOTLIGHT_DATE_PICKER(
        screen = TutorialScreen.TIMELINE,
        title = "Date Picker",
        description = "Tap the date container to select and view usage logs from previous dates."
    ),
    SPOTLIGHT_SUMMARY_BAR(
        screen = TutorialScreen.TIMELINE,
        title = "Usage Summary Bar",
        description = "Collapse the sheet and tap the summary bar to open highlights."
    ),
    SPOTLIGHT_HIGHLIGHT_METRICS(
        screen = TutorialScreen.HIGHLIGHT,
        title = "Usage Analytics",
        description = "Review overall screen time trends and category metrics."
    ),
    SPOTLIGHT_MASTER_CAPTURE_TOGGLE(
        screen = TutorialScreen.SETTINGS,
        title = "Master Capture Toggle",
        description = "Enable or disable global usage tracking and screenshot logging."
    ),
    SPOTLIGHT_EXCLUSION_LIST(
        screen = TutorialScreen.SETTINGS,
        title = "App Exclusion List",
        description = "Specify apps that should be ignored by the tracking engine."
    ),
    SPOTLIGHT_RETENTION_DURATION(
        screen = TutorialScreen.SETTINGS,
        title = "Data Retention Settings",
        description = "Configure auto-deletion thresholds for local screenshots."
    ),
    COMPLETED(
        screen = TutorialScreen.TIMELINE,
        title = "Tutorial Complete",
        description = "You're all set to use Timeline!"
    )
}