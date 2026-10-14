# Permission Flow Fix and Accessibility Notification

The objective is to ensure that the Accessibility permission card is correctly shown during onboarding, and that users are prompted for any missing permissions (including Accessibility) on subsequent app launches if they were missed or revoked. Additionally, a notification should be triggered if the Accessibility permission is missing.

## User Review Required

- The current implementation only checks permissions during the initial onboarding. I will modify this to check on every launch.
- If accessibility is missing, I will trigger a notification.

## Proposed Changes

### [Component Name]

#### [MODIFY] [PermissionViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/PermissionViewModel.kt)
- Update `checkPermissions` to be called more frequently, possibly on ViewModel initialization or when the app resumes.
- Ensure the state correctly reflects permissions at all times, even after onboarding.

#### [MODIFY] [AppNavigation.android.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/navigation/AppNavigation.android.kt)
- Update the navigation logic to re-check permissions upon app resume, potentially redirecting to the permission screen if required permissions are missing.

#### [NEW] [PermissionNotificationHandler.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/PermissionNotificationHandler.kt)
- A new component to handle showing a notification if accessibility permissions are missing.

## Verification Plan

### Automated Tests
- N/A

### Manual Verification
- Launch the app with all permissions revoked. Ensure the permission screen is shown.
- Grant permissions, exit, revoke them in settings, re-launch. Ensure it redirects back to the permission screen or notifies the user.
- Verify notification is shown if permissions are missing.
