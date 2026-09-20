# Implementation Plan - Highlights Delivery Settings & Onboarding Prep

This plan outlines the design and implementation strategy for moving highlight delivery preferences (frequency, delivery times up to max 6, and reasoning mode) into the Settings screen and laying the groundwork for capturing these preferences during the onboarding flow in future phases.

## User Review Required

> [!IMPORTANT]
> - **Settings Integration**: Moving mode selection (`HighlightReasoningMode`), digest frequency (1 to 6), and digest hours selection into `SettingsScreen`.
> - **Max 6 Digest Times**: Supporting up to 6 custom digest hours and frequency selections in both `UserPreferences` and `SettingsViewModel`.
> - **Onboarding Future-Proofing**: Exposing preferences state & event handlers so onboarding flows can seamlessly integrate preference selection in phase 2.

## Proposed Changes

### Shared Module: Presentation & Domain

#### [MODIFY] [UserPreferences.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/UserPreferences.kt)
- Update `PreferencesState` and `UserPreferences` to robustly support `highlightReasoningMode`, `digestFrequency` (1 to 6), and `digestHours` (`List<Int>`).
- Add helper methods `setHighlightReasoningMode` and `setDigestSchedule`.

#### [MODIFY] [SettingsContract.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/SettingsContract.kt)
- Add `highlightReasoningMode`, `digestFrequency`, and `digestHours` to `SettingsState`.
- Add new `SettingsEvent` types: `SetReasoningMode(mode)`, `SetDigestFrequency(frequency)`, `SetDigestHours(hours)`.

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/SettingsViewModel.kt)
- Expose reasoning mode, frequency, and hours through `SettingsState`.
- Handle `SetReasoningMode`, `SetDigestFrequency`, and `SetDigestHours` events by updating `UserPreferences`.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/SettingsScreen.kt)
- Add a new "HIGHLIGHTS & REASONING" settings category.
- Add UI controls for:
  - **Reasoning Mode Selector** (Concise, Balanced, Explanatory).
  - **Digest Frequency Slider / Selector** (1 to 6 times per day).
  - **Digest Hours Configuration** (selecting specific times of day for receiving highlights).

### Android App Module: Workers & Schedulers

#### [MODIFY] [DigestScheduler.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/kotlin/com/timeline/worker/DigestScheduler.kt)
- Dynamically schedule target hour work based on user-configured `digestHours` from `UserPreferences` instead of hardcoded `listOf(12, 17, 21)`.

## Verification Plan

### Automated Tests
- Run Gradle build to verify compilation across common and android modules.
- Check gradle test tasks.

### Manual Verification
- Deploy to device/emulator and verify:
  1. Settings screen displays Highlight & Reasoning options correctly.
  2. Changing reasoning mode, frequency, and times persists in DataStore and updates the worker schedule.
  3. Clean onboarding preparation ready for Phase 2.
