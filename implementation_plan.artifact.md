# Implementation Plan - App Identity, Exclusions & "Threaded" UI

This plan addresses the remaining core features: persistent exclusion policies, real app identity (names/icons), and a Material 3 "Threaded" UI polish with edge-to-edge support.

## User Review Required

> [!IMPORTANT]
> **Threaded UI Layout**: The Timeline will be redesigned to show a vertical line with dots on the left side, aligned with the center of each session card. The time will be displayed next to the dot, separate from the card itself.

> [!IMPORTANT]
> **Process Isolation**: Moving `TrackingService` to `:tracking` process requires that the Room database and DataStore are thread-safe across processes. I will use `DataStore` with its built-in multi-process support.

## Proposed Changes

### [shared] Domain & Data Layer

#### [MODIFY] [ExclusionPolicy.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/ExclusionPolicy.kt)
Convert to a reactive policy using `DataStore<Preferences>` to persist user-defined exclusions.

#### [NEW] [AppInfoProvider.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/AppInfoProvider.kt)
Interface to resolve app names and icons from package names.

#### [NEW] [AndroidAppInfoProvider.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/domain/AndroidAppInfoProvider.kt)
Android implementation using `PackageManager`.

---

### [shared] Presentation & UI

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/SettingsViewModel.kt)
Implement `ToggleExclusion` logic to update `DataStore`.

#### [MODIFY] [TimelineViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/TimelineViewModel.kt)
- Integrate `AppInfoProvider` to enrich `Session` objects with human-readable names.
- Implement filtering logic for `TimeFilter` (Today, Morning, Afternoon, Evening).

#### [MODIFY] [App.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/App.kt)
- **Threaded Layout**: Implement a custom layout where a vertical line and dots are drawn to the left of the session cards.
- **Edge-to-Edge**: Wrap content in `Scaffold` and use `WindowInsets` to handle system bars.
- Add filter chips at the top as shown in the mockup.

#### [MODIFY] [SessionDetailSheet.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/SessionDetailSheet.kt)
- Replace placeholders with real icons.
- Add horizontal scrollable screenshots.
- Implement "Started", "Ended", and "Duration" summary.
- Add "Previous/Next" navigation with app icons.

---

### [:androidApp] Configuration

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/AndroidManifest.xml)
- Set `android:process=":tracking"` for `TrackingService`.
- Configure distinct app icon resources.

---

## Verification Plan

### Automated Tests
- **ExclusionPolicyTest**: Verify `DataStore` updates.
- **TimelineViewModelTest**: Verify `TimeFilter` logic and app name resolution.

### Manual Verification
1. Verify the "Threaded" UI looks consistent with `img_1.png` mockup.
2. Toggle "Chrome" exclusion in Settings and verify it disappears from Timeline.
3. Check Edge-to-Edge behavior on physical device/emulator.
