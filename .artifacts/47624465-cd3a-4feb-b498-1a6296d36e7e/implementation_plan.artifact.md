# Use ic_launcher for Notification Icons

Update `TrackingNotificationHelper` to use the app's `ic_launcher` instead of system-provided icons for all notifications.

## User Review Required

> [!IMPORTANT]
> Launcher icons (`ic_launcher`) are typically colorful. Android design guidelines recommend that notification **small icons** be monochrome (white on transparent). Using a colorful launcher icon may result in the icon appearing as a solid white shape on some Android versions.

## Proposed Changes

### :androidApp

#### [MODIFY] [TrackingNotificationHelper.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/TrackingNotificationHelper.kt)
- Remove `import android.R` to allow local resource resolution.
- Update `setSmallIcon` calls in `buildNotification`, `showAccessibilityLostNotification`, and `showDigestNotification` to use `R.mipmap.ic_launcher`.

## Verification Plan

### Automated Tests
- I will verify that the project still compiles after the resource reference change.
- `gradlew :androidApp:assembleDebug`

### Manual Verification
- Deploy the app to a device/emulator.
- Trigger a notification (e.g., start tracking or wait for a digest) and verify that the app icon appears in the status bar.
