# Add Google Authentication with Local Sync

This plan outlines the integration of Google Authentication using Firebase Auth and Credential Manager into the Timeline project, following the provided reference snippet and maintaining a clean architecture.

## User Review Required

> [!IMPORTANT]
> The implementation uses Android-specific libraries (Firebase Auth Android SDK, Credential Manager). While the UI and ViewModel are in `commonMain`, the authentication logic will be housed in `androidMain`.

> [!NOTE]
> "Local sync" will be interpreted as preparing the infrastructure to sync local `Session` data with a remote store (like Firestore) once authenticated. The initial implementation will focus on the Auth flow and providing the authenticated user to the `TimelineRepository`.

## Proposed Changes

### Domain Layer (shared/commonMain)

#### [NEW] [User.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/model/User.kt)
- Define the `User` data class (renamed from `DomainUser`).

#### [NEW] [AuthRepository.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/repository/AuthRepository.kt)
- Define the `AuthRepository` interface.

#### [NEW] [SignInWithGoogleUseCase.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/usecase/SignInWithGoogleUseCase.kt)
- Implement the use case for signing in with Google.

---

### Data & Util Layer (shared/androidMain)

#### [NEW] [AuthRepositoryImpl.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/data/repository/AuthRepositoryImpl.kt)
- Implement `AuthRepository` using `FirebaseAuth`.

#### [NEW] [CredentialManagerHelper.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/util/CredentialManagerHelper.kt)
- Helper for interacting with Android's Credential Manager.

---

### Presentation Layer (shared/commonMain)

#### [NEW] [AuthViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/auth/AuthViewModel.kt)
- ViewModel to manage authentication state and handle Google ID tokens.

#### [NEW] [AuthUiState.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/auth/AuthUiState.kt)
- Sealed interface for Auth UI states.

---

### UI Layer (shared/commonMain)

#### [NEW] [LoginScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/auth/LoginScreen.kt)
- Compose UI for the sign-in screen.

---

### Dependency Injection (shared)

#### [MODIFY] [Koin Modules](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/di/Koin.kt)
- Register `AuthViewModel`, `SignInWithGoogleUseCase`, and `AuthRepository`.
- (If separate files exist, update `AppModule.kt` or create `AuthModule.kt`).

---

### Dependencies (shared/build.gradle.kts)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/build.gradle.kts)
- Add Firebase Auth and Credential Manager dependencies to `androidMain`.

---

### Integration & Sync

#### [MODIFY] [TimelineRepository.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/data/TimelineRepository.kt)
- Inject `AuthRepository` to allow user-aware data operations.

## Verification Plan

### Automated Tests
- Run `gradlew :shared:test` to verify domain logic and ViewModels.
- (Optional) Instrumented tests for `AuthRepositoryImpl`.

### Manual Verification
- Deploy `androidApp` to a device.
- Navigate to the Login screen.
- Tap "Sign in with Google".
- Verify successful authentication and UI state transition.
- Check logs (Kermit) for success/failure messages.
