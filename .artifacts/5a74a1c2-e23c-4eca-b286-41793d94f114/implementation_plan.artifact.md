# Implementation Plan - Clean up TrackingService.kt

This plan aims to refactor `TrackingService.kt` by extracting its core responsibilities into dedicated helper classes. This will improve maintainability, testability, and readability of the tracking logic.

## Proposed Changes

### [Component] Service Helpers

We will extract logic into three main helpers:
1. **`TrackingNotificationHelper`**: Manages all notification-related tasks (channels, foreground notifications, and alerts).
2. **`SessionManager`**: Manages the lifecycle of tracking sessions and coordinates with `WorkManager` for screenshots.
3. **`UsageStatsHelper`**: Encapsulates the logic for querying `UsageStatsManager` and detecting app transitions.

---

#### [NEW] [TrackingNotificationHelper.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/TrackingNotificationHelper.kt)
- Move `createNotificationChannel`, `buildNotification`, and `showAccessibilityLostNotification` here.
- Add constants for notification IDs and channel ID.

#### [NEW] [SessionManager.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/SessionManager.kt)
- Move `startNewSession`, `closePreviousSession`, and `enqueueScreenshot` here.
- Will require `TimelineRepository` and `Context` (for `WorkManager`).

#### [NEW] [UsageStatsHelper.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/UsageStatsHelper.kt)
- Move `pollUsageStats` logic here.
- Will require `UsageStatsManager`, `ExclusionPolicy`, and `UserPreferences`.
- It will callback to `SessionManager` when transitions are detected.

#### [MODIFY] [TrackingService.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/TrackingService.kt)
- Remove the extracted functions.
- Inject the new helpers (or instantiate them if DI isn't preferred for these specific helpers).
- Keep the `Service` lifecycle methods (`onCreate`, `onStartCommand`, `onTaskRemoved`, `onDestroy`) and the `trackingJob` loop.
- Use `TrackingNotificationHelper` for foreground and alerts.
- Use `UsageStatsHelper` and `SessionManager` within the polling loop.

## Verification Plan

### Automated Tests
- Since these are mostly Android-dependent components, manual verification on a device is primary.
- Unit tests for `UsageStatsHelper` could be added if I mock `UsageStatsManager`.

### Manual Verification
- Deploy the app and ensure tracking still starts automatically.
- Verify the foreground notification appears.
- Verify sessions are still recorded in the database (via logs).
- Verify screenshots are still enqueued.
- Verify the "Accessibility Lost" notification triggers when accessibility is disabled.
