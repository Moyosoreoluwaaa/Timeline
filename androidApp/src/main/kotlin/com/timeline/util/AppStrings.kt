package com.timeline.util

object AppStrings {
    const val AppName = "Timeline"

    // Auth Screen
    const val AuthSignInGoogle = "Sign in with Google"
    const val AuthDontHaveAccount = "Don't have an account?"
    const val AuthSignInWithGoogleLink = "Sign in with Google"

    // Onboarding Intro
    const val OnboardingWelcomeTitle = "Welcome to $AppName"

    const val OnboardingStackTitle = "Permissions Setup"
    const val OnboardingStackSubtitle = "Enable permissions to activate your automated timeline."

    // Three Permissions Overview
    const val ThreePermissionsTitle = "Three permissions. One timeline."
    const val ThreePermissionsSubtitle = "Timeline only works with all three. Each does one job."
    const val ThreePermissionsButton = "Set up Timeline"
    const val ThreePermissionsFootnote = "You can exclude any app later in Settings."

    const val PermAccessibilityJob = "Knows which app is open."
    const val PermAccessibilityLimit = "Never reads what you type."
    const val PermUsageJob = "Counts the minutes in each app."
    const val PermNotificationsJob = "Tells you when a highlight is ready."
    const val PermNotificationsLimit = "Up to three a day."

    // Accessibility Flow

    // Usage Flow

    // Notifications Flow

    // Final Step

    // Buttons
    const val ButtonNext = "Next"
    const val ButtonGrantAccess = "Grant Access"
    const val ButtonContinue = "Continue"
    const val ButtonAlreadyGranted = "Already Granted"

    // Legacy / Other

    // Permission Details
    const val PermissionUsageTitle = "Usage access"
    const val PermissionUsageDesc = "Analyze how you spend time."
    const val PermissionUsageIllustration = "illustrations/usage_access.png"
    const val PermissionNotificationsTitle = "Notifications"
    const val PermissionNotificationsDesc = "So we can tell you when your highlight is ready."
    const val PermissionNotificationsIllustration = "illustrations/notifications.png"
    const val PermissionAccessibilityTitle = "Accessibility Service"
    const val PermissionAccessibilityDesc = "Lets Timeline see which app is open. It never reads what you type."
    const val PermissionAccessibilityIllustration = "illustrations/accessibility.png"

    // Intro carousel
    const val IntroPage1Title    = "Where did your day go?"
    const val IntroPage1Subtitle = "See how your time really adds up."

    const val IntroPage2Title    = "Your day, as a story"
    const val IntroPage2Subtitle = "Friendly recaps, not endless lists."

    const val IntroPage3Title    = "You're in control"
    const val IntroPage3Subtitle = "Choose which apps stay private."

    const val IntroPage4Title    = "We'll come to you"
    const val IntroPage4Subtitle = "Highlights arrive right on time."

    const val IntroPage5Title    = "How much detail?"
    const val IntroPage5Subtitle = "Pick the style that feels right."

    const val ButtonSkip = "Skip"

    // Timeline Screen
    const val TimelineTitle = "Timeline"
    const val TimelineNoActivity = "Nothing captured yet. Use your phone as usual and your first story arrives at 12 PM."
    const val TimelineTotalUsage = "Total usage"
    const val TimelineSessionsCount = "Sessions"
    const val TimelineMostUsed = "Most used"
    const val TimelineOk = "OK"
    const val TimelineCancel = "Cancel"
    const val CommonVersion = "Version %s"
    const val CommonInsight = "Insight"
    const val CommonCollapse = "Collapse"
    const val CommonH = "h"
    const val CommonM = "m"

    // Highlight Screen
    const val HighlightTitle = "Highlight"
    const val HighlightPickDate = "Pick Date"
    const val HighlightTags = "Tags"
    const val HighlightContentStarts = "Content Starts"
    const val HighlightSessionEnds = "Session Ends"
    const val HighlightNoHighlights = "No Highlights Yet"
    const val HighlightCopied = "Copied to clipboard"
    const val HighlightSmartActive = "Active (Starburst)"
    const val HighlightPenToggle = "Pen (Toggle Reasoning)"
    const val HighlightGrantUsageAccess = "Grant Usage Access"
    const val HighlightAnalyzingContent = "Analyzing content in background..."
    const val HighlightPreloadingQueued = "Preloading queued"
    const val HighlightSanitizingPii = "Sanitizing PII data (Privacy Shield active)..."
    const val HighlightDeduplicating = "Deduplicating session captures..."
    const val HighlightContextualizing = "Injecting historical & contextual timeline..."
    const val HighlightReasoning = "Synthesizing Gemini executive narrative..."
    const val HighlightAnalyzingUsage = "Analyzing usage activity & generating executive narratives..."
    const val HighlightGeminiEngine = "Gemini Reasoning Engine"
    const val HighlightAgentNarrativeInsight = "AI Narrative Insight • %s"
    const val HighlightDailyNarrative = "Daily Executive Narrative • All Apps"
    const val HighlightPrivacyShieldActive = "Privacy Shield Active"
    const val HighlightPrivacyShieldDesc = "PII Redactor successfully detected and scrubbed sensitive data (emails, phone numbers, auth tokens, cards) locally on-device before Gemini reasoning."
    const val HighlightGotIt = "Got it"
    const val HighlightSanitized = "Sanitized"
    const val HighlightTasksNextSteps = "Detected Tasks & Next Steps"
    const val HighlightCopyTasks = "Copy Tasks"
    const val HighlightGenerateSmartNarrativeApp = "Generate Smart Narrative for this app"
    const val HighlightSynthesizeDailyNarrative = "Synthesize Daily Gemini Narrative"
    const val HighlightAgentActivityReasoning = "AI Activity Reasoning"
    const val HighlightAgentReasoningDesc = "Turn your raw screen captures into meaningful narratives. Data is processed via Gemini Cloud for reasoning. No data is used for model training."
    const val HighlightEnableSmartNarratives = "Enable Smart Narratives"
    const val HighlightNoActivityCaptured = "No activity captured yet. Use your apps or tap Sync to load your recent timeline activity."
    const val HighlightRawCaptures = "Raw Captures"
    const val HighlightReasoningDots = "Reasoning..."
    const val HighlightAgentSynthesis = "AI Synthesis"

    /** Step messages shown in ArcHighlightLoadingScreen while the highlight is being built. */
    val HighlightLoadingSteps = listOf(
        "Scanning your sessions",
        "Reading what you worked on",
        "Grouping your app switches",
        "Pulling out the key moments",
        "Writing your highlight"
    )

    // Real Data & Device Sync
    const val RealDataSyncButton = "Sync Real Data"
    const val RealDataPermissionBannerTitle = "Usage Access Required"
    const val RealDataPermissionBannerDesc = "Grant Usage Access so Timeline can track and analyze your real device app activity."
    // Insights Dashboard
    const val InsightsOverview = "Overview"
    const val InsightsApps = "Apps"
    const val InsightsTrends = "Trends"
    const val InsightsPatterns = "Patterns"
    const val InsightsUsageUp = "Your usage is up"
    const val InsightsUsageDown = "Your usage is down"
    const val InsightsMostUsedApp = "Most used app"
    const val InsightsMostActiveDay = "Most active day"
    const val InsightsTopAppsDesc = "Here are your top apps by usage."
    const val InsightsSeeAllApps = "See all %d apps"
    const val InsightsUsageTrend = "USAGE TREND"
    const val InsightsUsageMoreThanLast = "You're using your phone more than last week."
    const val InsightsWhenUsePhone = "WHEN YOU USE YOUR PHONE"
    const val InsightsUsageAfterSeven = "of your usage happens after 7 PM."
    const val InsightsFoundPatterns = "We found a few patterns in your usage."
    const val InsightsPeakAtNight = "Your phone time peaks at night"
    const val InsightsMostActiveHour = "%d PM is your most active hour."
    const val InsightsYouSpent = "You spent %s."
    const val InsightsLongSessions = "%s dominates your long sessions"
    const val InsightsLongestSessionWas = "Your longest session was %s."
    const val InsightsHeaviestDays = "%s are your heaviest days"
    const val InsightsHeaviestDayUsage = "%s of usage."
    const val InsightsExploreAllPatterns = "Explore all patterns"

    // Session Detail
    const val SessionStarted = "Started"
    const val SessionEnded = "Ended"
    const val SessionDuration = "Duration"
    const val SessionPrev = "Prev"
    const val SessionNext = "Next"
    const val SessionNoScreenshots = "No screenshots available"

    // Settings
    const val SettingsTitle = "Settings"
    const val SettingsCategoryPreference = "Preference"
    const val SettingsCategoryTracking = "Tracking"
    const val SettingsCategoryData = "Data"
    const val SettingsCategorySupport = "Support"
    const val SettingsHighlightsReasoning = "Highlights & Reasoning"
    const val SettingsManageSubscription = "Manage Subscription"
    const val SettingsUsageTrackingTitle = "Track app usage"
    const val SettingsScreenshotCaptureTitle = "Capture screens"
    const val SettingsAppExclusionsTitle = "Excluded apps"
    const val SettingsDataRetentionTitle = "Data retention"
    const val SettingsReasoningMode = "Reasoning Mode"
    const val SettingsFrequencyLabel = "%d times daily"
    const val SettingsDaysLabel = "%d days"
    const val SettingsAppVersionValue = "1.0.0 (9) - Stable"
    const val SettingsContactUsTitle = "Contact Support"
    const val SettingsReportBugsTitle = "Report a bug"
    const val SettingsUpgradeTitle = "You're a free user, get more"
    const val SettingsMadeByMo = "Made with ❤ by Mo"
    const val SettingsUnlockPro = "Unlock with Pro"

    // Notifications
    const val NotificationTrackingActiveTitle = "$AppName Tracking Active"
    const val NotificationTrackingActiveContent = "Recording activity journal..."
    const val NotificationAccessibilityLostTitle = "Screenshot capture stopped"
    const val NotificationAccessibilityLostContent = "Tap to re-enable accessibility permission"

    // Paywall
    const val PaywallUnlimitedInsights = "Unlimited insights."
    const val PaywallOneSimplePlan = "One simple plan."
    const val PaywallUnlockDeeper = "Unlock deeper insights."
    const val PaywallGoBeyond = "Go beyond 14 days and keep your data longer."
    const val PaywallLimitedTime = "LIMITED TIME"
    const val Paywall50Off = "20% off for 14 days"
    const val PaywallThenPrice = "Then $3.99 / month"
    const val PaywallPro = "PRO"
    const val PaywallTimelinePro = "Timeline PRO"
    const val PaywallMonthlyPrice = "$3.99 / month"
    const val PaywallUnlimitedHistory = "Unlimited history"
    const val PaywallDailyMonthlyDigest = "Daily & monthly digest"
    const val PaywallAdvancedInsights = "Advanced insights & trends"
    const val PaywallPrioritySupport = "Priority support"
    const val PaywallMonthlyOption = "Monthly"
    const val PaywallMonthlyBilling = "$3.99 / billed monthly"
    const val PaywallYearlyOption = "Yearly"
    const val PaywallYearlyBilling = "$38.33 / pay after free trial"
    const val PaywallSave50 = "Save 20%"
    const val PaywallStartTrial = "Start 7-Day Free Trial"
    const val PaywallCancelAnytime = "Cancel anytime. No commitments."
    const val PaywallRestorePurchases = "Restore Purchases"
    const val PaywallPrivacyPolicy = "Privacy Policy"
    const val PaywallTermsOfUse = "Terms of Use"
    // Style-specific headlines (previously hardcoded in each style file)
    const val PaywallFeatureGridTitle = "See everything, all the time."
    const val PaywallFeatureGridSubtitle = "Everything you need to understand your habits."
    const val PaywallComparisonTitle = "Free vs PRO."
    const val PaywallComparisonSubtitle = "See exactly what you unlock."

    // Accessibility / Content Descriptions
    const val ContentDescClose = "Close"
    const val ContentDescBack = "Back"
    const val ContentDescPrevSession = "Previous Session"
    const val ContentDescNextSession = "Next Session"
    const val ContentDescSessionSegment = "Session segment"

    // Permission Screen
}
