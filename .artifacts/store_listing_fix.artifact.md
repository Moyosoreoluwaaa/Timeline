# Play Store Listing Fix - Accessibility API Compliance

This artifact contains the updated store listing metadata for Google Play to comply with the Accessibility API policy.

## Metadata Summary

| Field | Current | Updated |
| :--- | :--- | :--- |
| **App Name** | Timeline | Timeline |
| **Short Description** | See your day unfolds, with automatic screenshots and a simple activity timeline. | See your day unfold with automatic screenshots and a simple, private activity timeline. |
| **Full Description** | [See below] | [See below - includes mandatory Accessibility API disclosure] |

## Full Description (Updated)

```text
Timeline — See Your Day as It Happened

Your phone is part of almost everything you do — but it can be surprisingly difficult to remember what you actually did throughout the day.

Timeline gives you a simple visual record of your phone activity, helping you look back at your day without digging through individual apps or trying to remember everything yourself.

[IMPORTANT] Accessibility API Usage Disclosure
Timeline requires the use of the AccessibilityService API to provide its core functionality of automated activity tracking. We use this API to:
• Detect when you open or switch between apps.
• Trigger automated screenshot captures of your active app sessions.

Our commitment to your privacy and safety:
• We do NOT change any user settings or prevent the ability for users to disable or uninstall the app.
• We do NOT work around Android's built-in privacy controls and notifications.
• We do NOT leverage the UI in a deceptive way.
• We do NOT record remote call audio.
• All data captured via the Accessibility API stays securely on your device and is not shared or sold.

Capture your activity
After setup and the required permissions are granted, Timeline runs quietly in the background. Using the AccessibilityService API, it identifies when you are interacting with apps to automatically capture screenshots, giving you a clear visual record of your moments and interactions.

See your screenshots
Timeline keeps the screenshots it captures organized so you can easily see what was recorded. Instead of manually taking screenshots and later wondering where they went, Timeline brings them together as part of your activity history.

Understand your phone usage
The Insights screen gives you an overview of your activity over time. See things such as:
• How much you've used your phone
• How many sessions you've had
• Which apps you've used most
• How your activity changes throughout the day

The goal isn't to overwhelm you with charts and numbers. It's to give you a simple picture of how your day unfolded.

Built to stay out of your way
Timeline is designed to work quietly while you go about your day. Once setup is complete, you don't need to constantly open the app for it to record your activity.

Your activity, your control
Timeline gives you full control over the features that require access to your device activity and screenshots. You can manage these settings at any time from within the app.

Timeline is designed around a simple idea:
Your phone already contains a record of your day. Timeline helps you see it.
```

## Mandatory Action for Developer
> [!IMPORTANT]
> You must also provide a link in the Play Console to a video showcasing the core functionality that uses the AccessibilityService API. This video must clearly show the user granting permission and the app then performing its automated tracking.
