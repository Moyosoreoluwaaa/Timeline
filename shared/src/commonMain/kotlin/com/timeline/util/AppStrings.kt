package com.timeline.util

object AppStrings {
    const val AppName = "Timeline"
    
    // Auth Screen
    const val AuthWelcomeTitle = "Welcome to $AppName"
    const val AuthWelcomeSubtitle = "Turn your app usage into clarity."
    const val AuthSignInGoogle = "Sign in with Google"
    const val AuthSignInApple = "Sign in with Apple"
    const val AuthOr = "or"
    const val AuthEmailPlaceholder = "Email address"
    const val AuthDontHaveAccount = "Don't have an account?"
    const val AuthSignInWithGoogleLink = "Sign in with Google"

    // Onboarding Intro (Unified Screen 1)
    const val OnboardingWelcomeTitle = "Welcome to $AppName"
    const val OnboardingWelcomeSubtitle = "A quiet, private timeline of your phone activity—so you can understand your day, not lose it."
    const val OnboardingValueProp1Title = "Automatic daily story"
    const val OnboardingValueProp1Desc = "Timeline quietly records your app sessions so you get a clear, honest picture of where your time goes."
    const val OnboardingValueProp2Title = "Helpful highlights"
    const val OnboardingValueProp2Desc = "See meaningful moments, trends, and summaries without tedious manual journaling."
    const val OnboardingGetStarted = "Get Started"
    
    // Permission Card Stack (Screen 2)
    const val OnboardingStackTitle = "Set up your timeline"
    const val OnboardingStackSubtitle = "Enable permissions to start building your personal activity journal."

    // Privacy Summary (Screen 3)
    const val OnboardingPrivacyTitle = "Your Privacy & Data"
    const val OnboardingPrivacySubtitle = "Transparency is at our core. Here is how we handle your information."
    const val OnboardingPrivacyDataImprovement = "Data and usage metrics might be collected to improve app functionality, optimize features, and provide you with the best experience."
    const val OnboardingPrivacyOnDevice = "Most processing happens directly on your device to keep your personal timeline private."
    const val OnboardingPrivacyYouInControl = "You're in full control. You can pause tracking or delete your data at any time in Settings."
    const val OnboardingPrivacyAgreeNotice = "By continuing, you agree to our terms and understand that some data might be collected to deliver and improve our services."

    // Mode Selection (Screen 3)
    const val OnboardingModeTitle = "Choose your experience"
    const val OnboardingModeSubtitle = "Pick the plan that fits how you want to reflect on your time."
    const val OnboardingBasicModeTitle = "Basic Mode"
    const val OnboardingBasicModeDesc = "Essential screen time logging, on-device timelines, and simple app usage summaries."
    const val OnboardingBasicModeButton = "Continue with Basic"
    const val OnboardingProModeTitle = "Timeline Pro"
    const val OnboardingProModeBadge = "RECOMMENDED"
    const val OnboardingProModeDesc = "Rich visual highlights, smart search through your day, proactive gentle nudges, and deep cloud-powered story recaps."
    const val OnboardingProModeButton = "Unlock Pro Highlights"


    // Accessibility Flow
    const val OnboardingAccessibilityIntroTitle = "Accessibility powers visual screenshots"
    const val OnboardingAccessibilityIntro1 = "See when you open or switch apps so we can record activity accurately."
    const val OnboardingAccessibilityIntro2 = "Capture visual screenshots to give you contextual summaries of your app sessions."
    const val OnboardingAccessibilityIntro3 = "Run smoothly in the background to keep your timeline complete."
    const val OnboardingAccessibilityIntroFooter = "Data and usage metrics might be collected to improve app functionality, optimize features, and provide you with the best experience."
    
    const val OnboardingAccessibilityGrantTitle = "Accessibility access"
    const val OnboardingAccessibilityGrantSubtitle = "Lets Timeline capture screenshots and observe app switches to build a visual timeline."
    
    const val OnboardingAccessibilitySuccessTitle = "Accessibility access enabled!"
    const val OnboardingAccessibilitySuccessSubtitle = "Timeline will now capture visual screenshots and app sessions."
    
    const val OnboardingAccessibilityFailureTitle = "Accessibility not granted"
    const val OnboardingAccessibilityFailureSubtitle = "Accessibility is optional. Without it, Timeline won't capture screenshots, but you can still track screen time with Usage Stats."

    // Usage Flow
    const val OnboardingUsageIntroTitle = "App Usage access for screen time"
    const val OnboardingUsageIntroSubtitle = "Usage access lets Timeline track which apps you use and for how long."
    const val OnboardingUsageIntro1 = "See total screen time and app durations."
    const val OnboardingUsageIntro2 = "Build daily and weekly usage trends."
    const val OnboardingUsageIntro3 = "Data might be collected to improve app functionality and service quality."
    const val OnboardingUsageIntro4 = "We track app names and usage duration to power your timeline."
    
    const val OnboardingUsageGrantTitle = "App Usage access"
    const val OnboardingUsageGrantSubtitle = "Lets Timeline see which apps you use and for how long."
    
    const val OnboardingUsageSuccessTitle = "App Usage access enabled!"
    const val OnboardingUsageSuccessSubtitle = "Timeline can now aggregate your screen time and app usage stats."
    
    const val OnboardingUsageFailureTitle = "Usage access not granted"
    const val OnboardingUsageFailureSubtitle = "You can still continue, but Timeline won't be able to query historical app usage durations."

    // Notifications Flow
    const val OnboardingNotificationsIntroTitle = "Stay informed with updates"
    const val OnboardingNotificationsIntroSubtitle = "Notifications keep Timeline active and deliver daily activity recaps."
    const val OnboardingNotificationsIntro1 = "Reminds you about your day with smart summaries."
    const val OnboardingNotificationsIntro2 = "Keeps your timeline up to date even in the background."
    const val OnboardingNotificationsIntro3 = "You're in control. Turn off notifications anytime in Settings."
    
    const val OnboardingNotificationsGrantTitle = "Notifications"
    const val OnboardingNotificationsGrantSubtitle = "Delivers smart summaries and maintains background tracking."

    // Final Step
    const val OnboardingAllSetTitle = "All set! Timeline is ready"
    const val OnboardingAllSetSubtitle = "We'll start building your timeline based on your granted permissions."
    const val OnboardingAllSet1 = "Private by default"
    const val OnboardingAllSet2 = "On-device processing"
    const val OnboardingAllSet3 = "You're in full control"
    const val OnboardingAllSetDataNotice = "Note: Data might be collected to improve app functionality and deliver optimal services."

    // Buttons
    const val ButtonNext = "Next"
    const val ButtonGrantAccess = "Grant Access"
    const val ButtonEnable = "Enable"
    const val ButtonContinue = "Continue"
    const val ButtonTryAgain = "Try Again"
    const val ButtonNotNow = "Not now"
    const val ButtonSkipForNow = "Skip for now"
    const val ButtonAlreadyGranted = "Already Granted — Next"
    const val ButtonMaybeLater = "Maybe later"
    const val ButtonOpenTimeline = "Open Timeline"
    const val ButtonUnderstandContinue = "I Understand & Continue"
    const val ButtonOpenSettings = "Open Settings"
    const val ButtonContinueSettings = "Continue to Settings"

    // Legacy / Other
    const val PermissionAllSetTitle = "You're all set!"
    const val PermissionAlmostThereTitle = "Almost there..."
    const val PermissionAllSetSubtitle = "Everything is ready for your timeline."
    const val PermissionAlmostThereSubtitle = "To show you real insights, we need access to how you use your apps."
    const val PermissionAllowButton = "Allow Access"
    const val PermissionNotNowButton = "Not Now"

    // Permission Details
    const val PermissionUsageTitle = "Usage access"
    const val PermissionUsageDesc = "Analyze how you spend time."
    const val PermissionUsageIllustration = "illustrations/usage_access.png"
    const val PermissionOverlayTitle = "Display over other apps"
    const val PermissionOverlayDesc = "Enable background screen capture support."
    const val PermissionOverlayIllustration = "illustrations/overlay.png"
    const val PermissionNotificationsTitle = "Notifications"
    const val PermissionNotificationsDesc = "Deliver important alerts and reminders."
    const val PermissionNotificationsIllustration = "illustrations/notifications.png"
    const val PermissionAccessibilityTitle = "Accessibility Service"
    const val PermissionAccessibilityDesc = "Powers advanced automated activity tracking."
    const val PermissionAccessibilityIllustration = "illustrations/accessibility.png"
    const val PermissionBatteryTitle = "Battery Optimization"
    const val PermissionBatteryDesc = "Prevents background service interruptions."
    const val PermissionBatteryIllustration = "illustrations/battery.png"
    
    // Timeline Screen
    const val TimelineTitle = "Timeline"
    const val TimelineToday = "Today"
    const val TimelineTimeOfDay = "Time of Day"
    const val TimelineSettings = "Settings"
    const val TimelineNoActivity = "No activity recorded"
    const val TimelineTotalUsage = "Total usage"
    const val TimelineSessionsCount = "Sessions"
    const val TimelineMostUsed = "Most used"
    const val TimelineOk = "OK"

    // Insights Dashboard
    const val InsightsOverview = "Overview"
    const val InsightsApps = "Apps"
    const val InsightsTrends = "Trends"
    const val InsightsPatterns = "Patterns"
    const val InsightsUsageUp = "Your usage is up"
    const val InsightsUsageDown = "Your usage is down"
    const val InsightsVsPrevious = "vs previous 7 days"
    const val InsightsBiggestPattern = "Your biggest pattern"
    const val InsightsMostUsedApp = "Most used app"
    const val InsightsMostActiveDay = "Most active day"
    const val InsightsWhereTimeGoes = "WHERE YOUR TIME GOES"
    const val InsightsTopAppsDesc = "Here are your top apps by usage."
    const val InsightsSeeAllApps = "See all %d apps"
    const val InsightsUsageTrend = "USAGE TREND"
    const val InsightsUsageMoreThanLast = "You're using your phone more than last week."
    const val InsightsWhenUsePhone = "WHEN YOU USE YOUR PHONE"
    const val InsightsUsageAfterSeven = "of your usage happens after 7 PM."
    const val InsightsFoundPatterns = "We found a few patterns in your usage."
    const val InsightsPeakAtNight = "Your phone time peaks at night"
    const val InsightsMostActiveHour = "%d PM is your most active hour."
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
    const val SettingsCategoryTracking = "TRACKING"
    const val SettingsCategoryCapture = "CAPTURE"
    const val SettingsCategoryPermissions = "PERMISSIONS"
    const val SettingsCategoryData = "DATA"
    const val SettingsCategoryAbout = "ABOUT"
    const val SettingsCategorySupport = "SUPPORT"
    const val SettingsUsageTrackingTitle = "Enable app usage tracking"
    const val SettingsUsageTrackingDesc = "Record app usage activity."
    const val SettingsScreenshotCaptureTitle = "Enable screenshot capture"
    const val SettingsScreenshotCaptureDesc = "Save snapshots during sessions."
    const val SettingsAppExclusionsTitle = "Manage app exclusions"
    const val SettingsAppExclusionsDesc = "Manage apps not to be recorded."
    const val SettingsDataRetentionTitle = "Adjust data retention period"
    const val SettingsDataRetentionDesc = "Keep data for %d days"
    const val SettingsDataRetentionSelect = "Select retention period"
    const val SettingsAboutTimelineTitle = "About the $AppName app"
    const val SettingsAboutTimelineDesc = "Learn more about the application."
    const val SettingsAppVersionTitle = "Application version"
    const val SettingsAppVersionValue = "1.0.0 (Stable)"
    const val SettingsContactUsTitle = "Contact us for support"
    const val SettingsContactUsDesc = "Get help or provide feedback."
    const val SettingsReportBugsTitle = "Report bugs and issues"
    const val SettingsReportBugsDesc = "Help us improve by reporting issues."
    const val SettingsUpgradeTitle = "You're a free user, get more"
    const val SettingsMadeByMo = "Made with love by Mo"
    
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
    const val PaywallAdvancedInsights = "Advanced insights"
    const val PaywallPrioritySupport = "Priority support"
    const val PaywallMonthlyOption = "Monthly"
    const val PaywallMonthlyBilling = "$3.99 / billed monthly"
    const val PaywallYearlyOption = "Yearly"
    const val PaywallYearlyBilling = "$38.33 / pay after free trial"
    const val PaywallSave50 = "Save 20%"
    const val PaywallStartTrial = "Start 7-Day Free Trial"
    const val PaywallCancelAnytime = "Cancel anytime. No commitments."

    // Accessibility / Content Descriptions
    const val ContentDescClose = "Close"
    const val ContentDescBack = "Back"
    const val ContentDescSettings = "Settings"
    const val ContentDescExpand = "Expand"
    const val ContentDescCollapse = "Collapse"
    const val ContentDescPrevSession = "Previous Session"
    const val ContentDescNextSession = "Next Session"
    const val ContentDescAppIcon = "App Icon"
    const val ContentDescScreenshot = "Screenshot"
    const val ContentDescSessionSegment = "Session segment"
}
