package com.timeline.tutorial

enum class TutorialScreen {
    TIMELINE,
    HIGHLIGHT,
    SETTINGS,
    PAYWALL
}

enum class TutorialStep(
    val screen: TutorialScreen,
    val title: String,
    val description: String
) {
    PREREQUISITE_CHECK(
        screen = TutorialScreen.TIMELINE,
        title = "Preparing Showcase",
        description = "Verifying mock usage data and captures..."
    ),
    // Step 1: App entry: tap a row to open the sheet.
    SPOTLIGHT_APP_ENTRY(
        screen = TutorialScreen.TIMELINE,
        title = "App Entry",
        description = "Tap any app to see what you did in it."
    ),
    // Step 2: Screenshot thumbnail and full-screen preview: tap to open, tap to close.
    SPOTLIGHT_SCREENSHOT_THUMBNAIL(
        screen = TutorialScreen.TIMELINE,
        title = "Captures Preview",
        description = "Tap to open, tap to close."
    ),
    FULL_SCREEN_IMAGE_PREVIEW(
        screen = TutorialScreen.TIMELINE,
        title = "Full-Screen Preview",
        description = "Tap anywhere to close the preview."
    ),
    // Step 3: Expand sheet and Prev/Next: drag up, then use the floating row.
    EXPAND_BOTTOM_SHEET(
        screen = TutorialScreen.TIMELINE,
        title = "Expand Sheet",
        description = "Drag up, then use the floating row."
    ),
    SPOTLIGHT_SESSION_NAVIGATOR(
        screen = TutorialScreen.TIMELINE,
        title = "Session Controls",
        description = "Drag up, then use the floating row."
    ),
    // Step 4: Summary bar and Highlights: tap the bar to open Highlights.
    SPOTLIGHT_SUMMARY_BAR(
        screen = TutorialScreen.TIMELINE,
        title = "Summary & Highlights",
        description = "Tap the bar to open Highlights."
    ),
    // Step 4b: Loading state — shown while highlights are being generated.
    SPOTLIGHT_HIGHLIGHT_LOADING(
        screen = TutorialScreen.HIGHLIGHT,
        title = "Analyzing Your Day",
        description = "Sit tight while we generate your personalized highlight cards."
    ),
    SPOTLIGHT_HIGHLIGHT_CARD(
        screen = TutorialScreen.HIGHLIGHT,
        title = "Daily Highlights",
        description = "Tap any highlight card to inspect details."
    ),
    // Step 5: Settings gear and capture toggle: this is your master privacy control.
    SPOTLIGHT_SETTINGS_ICON(
        screen = TutorialScreen.TIMELINE,
        title = "Privacy Controls",
        description = "This is your master privacy control."
    ),
    SPOTLIGHT_TRACKING_OPTIONS(
        screen = TutorialScreen.SETTINGS,
        title = "Tracking & Privacy",
        description = "This is your master privacy control."
    ),
    // Step 6: Manage App Exclusion: include the retention slider in the same spotlight.
    SPOTLIGHT_APP_EXCLUSIONS(
        screen = TutorialScreen.SETTINGS,
        title = "Excluded Apps & Retention",
        description = "Exclude private apps and adjust your data retention slider."
    ),
    SPOTLIGHT_EXCLUSIONS_SHEET(
        screen = TutorialScreen.SETTINGS,
        title = "Excluded Apps",
        description = "Toggle the switch next to any app to exclude it from your timeline."
    ),
    SPOTLIGHT_DATA_RETENTION(
        screen = TutorialScreen.SETTINGS,
        title = "Data Retention",
        description = "Choose your local capture retention period."
    ),
    SPOTLIGHT_RETENTION_SHEET(
        screen = TutorialScreen.SETTINGS,
        title = "Retention Period",
        description = "Set your preferred cleanup period."
    ),
    // Cut steps retained for backwards-safe enum compatibility
    SPOTLIGHT_TIME_FILTER_ICON(screen = TutorialScreen.TIMELINE, title = "", description = ""),
    SPOTLIGHT_TIME_FILTER_SECTION(screen = TutorialScreen.TIMELINE, title = "", description = ""),
    SPOTLIGHT_DATE_CONTAINER(screen = TutorialScreen.TIMELINE, title = "", description = ""),
    SPOTLIGHT_DATE_PICKER(screen = TutorialScreen.TIMELINE, title = "", description = ""),
    SPOTLIGHT_HIGHLIGHTS_PREFERENCE(screen = TutorialScreen.SETTINGS, title = "", description = ""),
    SPOTLIGHT_REASONING_SHEET(screen = TutorialScreen.SETTINGS, title = "", description = ""),
    GESTURE_DRAG_SETTINGS(screen = TutorialScreen.TIMELINE, title = "", description = ""),

    PRO_PAYWALL_STEP(
        screen = TutorialScreen.PAYWALL,
        title = "Timeline Pro Experience",
        description = "Unlock all features with Timeline Pro or dismiss to continue as Free."
    ),
    COMPLETED(
        screen = TutorialScreen.TIMELINE,
        title = "Tutorial Complete",
        description = "You're all set to use Timeline!"
    )
}