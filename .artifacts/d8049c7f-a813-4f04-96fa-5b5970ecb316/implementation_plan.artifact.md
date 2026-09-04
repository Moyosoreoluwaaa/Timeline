# Configure Email Channel for OneSignal

The goal is to enable the email channel in OneSignal by syncing the user's email address during the account synchronization process. This is a prerequisite for sending automated emails (like welcome emails) via OneSignal Journeys.

## User Review Required

> [!IMPORTANT]
> This change assumes that `OneSignal.User.addEmail(email)` is sufficient to "configure the channel" as per the user's requirement. In OneSignal SDK v5, this associates the email with the user and enables email as a communication channel.

## Proposed Changes

### Domain Layer

#### [MODIFY] [SyncUserAccountUseCase.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/usecase/SyncUserAccountUseCase.kt)
- Update `invoke` function to accept an optional `email: String?` parameter.
- Call `notificationManager.setEmail(email)` if the email is provided.

### Presentation Layer

#### [MODIFY] [AuthViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/AuthViewModel.kt)
- Pass the `user.email` from the successful sign-in result to `syncUserAccountUseCase`.

## Verification Plan

### Automated Tests
- I will check if there are existing tests for `SyncUserAccountUseCase` and update them if they exist.

### Manual Verification
- Deploy the app to a device.
- Perform a Google Sign-In.
- Check the logs to ensure "Firebase Auth success" is followed by account sync, and verify that `OneSignal.User.addEmail` would be called (via the `OneSignalManager` implementation).
