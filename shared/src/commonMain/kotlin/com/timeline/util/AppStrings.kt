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

    // Onboarding Intro
    const val OnboardingWelcomeTitle = "Welcome to $AppName"
    const val OnboardingWelcomeSubtitle = "A private timeline of your phone activity—so you can understand your time, not lose it."
    const val OnboardingValuePropTitle = "We build your activity timeline"
    const val OnboardingValuePropSubtitle = "Timeline quietly records when you use apps so we can show you a clear, honest view of your day."
    const val OnboardingOverviewTitle = "To make this work, we need 3 permissions"
    const val OnboardingOverviewSubtitle = "These let Timeline run in the background and keep your data accurate and private."

    // Accessibility Flow
    const val OnboardingAccessibilityIntroTitle = "Accessibility is required to build your timeline"
    const val OnboardingAccessibilityIntro1 = "See when you open or switch apps so we can record activity accurately."
    const val OnboardingAccessibilityIntro2 = "Understand what's on your screen to group and label your activity."
    const val OnboardingAccessibilityIntro3 = "Run in the background to keep your timeline complete."
    const val OnboardingAccessibilityIntroFooter = "Your data stays on your device. We never collect, share, or sell your information."
    
    const val OnboardingAccessibilityGrantTitle = "Accessibility access"
    const val OnboardingAccessibilityGrantSubtitle = "Lets Timeline observe what you're doing on your device so we can build an accurate timeline."
    
    const val OnboardingAccessibilitySuccessTitle = "Nice! Accessibility access is enabled"
    const val OnboardingAccessibilitySuccessSubtitle = "Timeline can now build a more accurate activity timeline."
    
    const val OnboardingAccessibilityFailureTitle = "Accessibility access not granted yet"
    const val OnboardingAccessibilityFailureSubtitle = "You can still use Timeline, but your timeline may be incomplete or less accurate."

    // Usage Flow
    const val OnboardingUsageIntroTitle = "We need to see app usage"
    const val OnboardingUsageIntroSubtitle = "Usage access helps Timeline understand which apps you use and when."
    const val OnboardingUsageIntro1 = "See which apps you use and how long."
    const val OnboardingUsageIntro2 = "Build daily and weekly insights from your activity."
    const val OnboardingUsageIntro3 = "Everything stays private and on your device."
    const val OnboardingUsageIntro4 = "We don't access content. We only collect app names and usage time."
    
    const val OnboardingUsageGrantTitle = "App Usage access"
    const val OnboardingUsageGrantSubtitle = "Lets Timeline see which apps you use and for how long."
    
    const val OnboardingUsageSuccessTitle = "Nice! Usage access is enabled"
    const val OnboardingUsageSuccessSubtitle = "Timeline can now see which apps you use and how long."
    
    const val OnboardingUsageFailureTitle = "Usage access not granted yet"
    const val OnboardingUsageFailureSubtitle = "You can still continue, but Timeline won't be able to show app usage or insights."

    // Notifications Flow
    const val OnboardingNotificationsIntroTitle = "Stay informed"
    const val OnboardingNotificationsIntroSubtitle = "Notifications keep Timeline running in the background."
    const val OnboardingNotificationsIntro1 = "Reminds you about your day with smart summaries."
    const val OnboardingNotificationsIntro2 = "Keeps your timeline up to date even when the app is in the background."
    const val OnboardingNotificationsIntro3 = "You're in control. You can turn off notifications anytime."
    
    const val OnboardingNotificationsGrantTitle = "Notifications"
    const val OnboardingNotificationsGrantSubtitle = "Keeps Timeline running in the background and reminds you about your day."

    // Final Step
    const val OnboardingAllSetTitle = "All set! Timeline is ready"
    const val OnboardingAllSetSubtitle = "We'll start building your timeline in the background."
    const val OnboardingAllSet1 = "Private by default"
    const val OnboardingAllSet2 = "On your device"
    const val OnboardingAllSet3 = "You're in control"

    // Buttons
    const val ButtonNext = "Next"
    const val ButtonGrantAccess = "Grant Access"
    const val ButtonEnable = "Enable"
    const val ButtonContinue = "Continue"
    const val ButtonTryAgain = "Try Again"
    const val ButtonNotNow = "Not now"
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
