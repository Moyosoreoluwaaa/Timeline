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
    SPOTLIGHT_SETTINGS_ICON(
        screen = TutorialScreen.TIMELINE,
        title = "Configure Preferences",
        description = "Tap the settings gear icon to customize tracking and data options."
    ),
    SPOTLIGHT_HIGHLIGHTS_PREFERENCE(
        screen = TutorialScreen.SETTINGS,
        title = "Highlights & Reasoning",
        description = "Tap this preference option to customize your Gemini narrative style and frequency."
    ),
    SPOTLIGHT_REASONING_BALANCED(
        screen = TutorialScreen.SETTINGS,
        title = "Balanced Narrative",
        description = "Select the 'Balanced' option for a mix of detail and conciseness in your highlights."
    ),
    SPOTLIGHT_REASONING_SLIDER(
        screen = TutorialScreen.SETTINGS,
        title = "Digest Frequency",
        description = "Use this slider to adjust how often Gemini generates your timeline highlights."
    ),
    SPOTLIGHT_TRACKING_OPTIONS(
        screen = TutorialScreen.SETTINGS,
        title = "Tracking Controls",
        description = "Toggle app interaction logging and screenshot captures here."
    ),
    SPOTLIGHT_APP_EXCLUSIONS(
        screen = TutorialScreen.SETTINGS,
        title = "Manage App Exclusion",
        description = "Tap here to exclude specific private apps from being tracked."
    ),
    SPOTLIGHT_EXCLUSION_MOCK_ITEM(
        screen = TutorialScreen.SETTINGS,
        title = "Exclude Apps",
        description = "Toggle the switch next to any app to prevent it from appearing in your timeline."
    ),
    SPOTLIGHT_DATA_RETENTION(
        screen = TutorialScreen.SETTINGS,
        title = "Adjust Data Retention",
        description = "Tap here to adjust how many days your local screenshots and logs are kept."
    ),
    SPOTLIGHT_RETENTION_SHEET_CONTENT(
        screen = TutorialScreen.SETTINGS,
        title = "Retention Period",
        description = "Drag the slider to set your preferred data cleanup frequency."
    ),
    SPOTLIGHT_SUMMARY_BAR(
        screen = TutorialScreen.TIMELINE,
        title = "Usage Summary Bar",
        description = "Collapse the sheet and tap the summary bar to open highlights."
    ),
    SPOTLIGHT_HIGHLIGHT_CARD(
        screen = TutorialScreen.HIGHLIGHT,
        title = "Interactive Highlights",
        description = "Tap any highlight card to inspect details and complete the tutorial."
    ),
    COMPLETED(
        screen = TutorialScreen.TIMELINE,
        title = "Tutorial Complete",
        description = "You're all set to use Timeline!"
    )
}
