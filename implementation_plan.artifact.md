# Implementation Plan - Timeline Core Activity & Tracking

This plan outlines the implementation of the Timeline core activity journal, including the independent foreground tracking service, persistence layer, and MVI-based UI.

## User Review Required

> [!IMPORTANT]
> **Persistence Layer Choice**: The project currently has **Room (KMP)** dependencies configured. Although SQLDelight was mentioned in the grilling session, Room KMP provides the same relational capabilities for multiplatform. I will proceed with Room unless you explicitly prefer switching to SQLDelight.

> [!IMPORTANT]
> **Separate Process Service**: The tracking service will run in `com.timeline:tracking`. This ensures it survives UI process terminations but requires careful consideration for shared memory (handled via the Database).

## Proposed Changes

### [shared] Domain & Data Layer

#### [MODIFY] [CONTEXT.md](file:///C:/Users/USER/AndroidStudioProjects/Timeline/CONTEXT.md)
Update with resolved terms if necessary.

#### [NEW] [Session.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/Session.kt)
Core domain model for activity sessions.

#### [NEW] [TimelineDatabase.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/data/TimelineDatabase.kt)
Room database definition for Multiplatform.

#### [NEW] [TimelineRepository.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/data/TimelineRepository.kt)
Repository interface and implementation for session management.

---

### [shared] Presentation Layer (MVI)

#### [NEW] [TimelineContract.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/TimelineContract.kt)
MVI Contract (State, Event, Effect) for the Timeline screen.

#### [NEW] [TimelineViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/TimelineViewModel.kt)
MVI ViewModel implementation.

---

### [:androidApp] Tracking Service

#### [NEW] [TrackingService.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/service/TrackingService.kt)
Independent foreground service running in `:tracking` process. Implements the **Adaptive** polling logic via `UsageStatsManager`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/AndroidManifest.xml)
Register the service with `android:process=":tracking"` and required permissions.

---

### [:androidApp] UI Components

#### [MODIFY] [App.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/App.kt)
Implement the main Timeline list and the **Platform Native Bottom Sheet** interaction.

#### [NEW] [SessionDetailSheet.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/SessionDetailSheet.kt)
The specialized draggable bottom sheet (Icon | Time | Expand) with screenshot gallery.

---

### [:androidApp] Background Tasks

#### [NEW] [SummaryWorker.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/worker/SummaryWorker.kt)
WorkManager implementation for the **Smart Trigger** daily summary notification.

## Verification Plan

### Automated Tests
- **ViewModel Tests**: Verify MVI state transitions using Turbine.
- **Repository Tests**: Verify CRUD operations on the Room database.

### Manual Verification
1. Deploy to Android device.
2. Grant Usage Access permission.
3. Open several apps (YouTube, Chrome, etc.).
4. Verify Timeline records sessions accurately.
5. Swipe away Timeline from Recents and verify tracking notification persists and continues recording.
6. Verify Bottom Sheet "Expand" interaction and screenshot display.
