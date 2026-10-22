# UI/UX Improvements Plan

This plan outlines the changes to format session durations, allow the session detail sheet to expand to full screen, and standardize the "Unlock with Pro" button in settings sheets.

## Proposed Changes

### Duration Formatting

Update `formatDuration` to follow the requested rules:
- `< 1 min`: "X secs"
- `1 min <= d < 60 min`: "Xm"
- `d >= 60 min`: "Xh Ym"

#### [MODIFY] [TimelineComponents.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/components/TimelineComponents.kt)
Update `formatDuration(minutes: Long, seconds: Long)` implementation.

#### [MODIFY] [HighlightSegmentBuilder.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/HighlightSegmentBuilder.kt)
Update `formatDuration(minutes: Long)` implementation.

---

### Session Detail Sheet Height

Ensure `SessionDetailSheet` can expand to the full screen height when expanded in `TimelineScreen`.

#### [MODIFY] [TimelineScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/TimelineScreen.kt)
- Remove top padding from `BottomSheetScaffold`.
- Pass `topPadding` to `TimelineSessionList`.

#### [MODIFY] [TimelineSessionList.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/TimelineSessionList.kt)
- Add `topPadding: Dp` parameter.
- Apply `topPadding` to the `LazyColumn` content padding.

---

### Unlock with Pro Button Standardization

Standardize the "Unlock with Pro" button across "Excluded apps", "Data retention", and "Reasoning mode" sheets.

#### [MODIFY] [AppExclusionsSheet.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/settings/AppExclusionsSheet.kt)
Center the "Unlock with Pro" button.

#### [MODIFY] [DataRetentionSheet.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/settings/DataRetentionSheet.kt)
- Add `onNavigateToPaywall: () -> Unit` parameter.
- Add centered "Unlock with Pro" button when feature is locked.

#### [MODIFY] [ReasoningModeSheet.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/settings/ReasoningModeSheet.kt)
Ensure the button matches the style and is centered.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/SettingsScreen.kt)
Pass `onNavigateToPaywall` to `DataRetentionSheet`.

## Verification Plan

### Manual Verification
- Check "Total session" and session durations in the timeline. Verify formats:
    - `45s` -> `45 secs`
    - `14m` -> `14m`
    - `98m` -> `1h 38m`
- Expand `SessionDetailSheet` and verify it covers the top bar.
- Open "Excluded apps", "Data retention", and "Reasoning mode" sheets as a free user and verify the centered "Unlock with Pro" button.
