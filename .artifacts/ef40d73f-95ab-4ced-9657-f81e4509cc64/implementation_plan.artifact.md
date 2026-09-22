# Restore Full Sheet Transitions & Elevate Full Screen Image Overlay

The goal is to fix issues introduced during the `ModalBottomSheet` migration:
1. **Restore expanded/partially expanded state**: Set `skipPartiallyExpanded = false` on `rememberModalBottomSheetState()` and synchronize with the ViewModel's `isSheetExpanded` state using `sheetState.expand()` / `sheetState.partialExpand()`.
2. **Smooth progress animation**: Animate the `expansionProgress` using `animateFloatAsState` based on `state.isSheetExpanded` to smoothly transition contents in `SessionDetailSheet`.
3. **Show Full Screen Image ABOVE the bottom sheet**: Render the full-screen image overlay inside a full-screen `Dialog` so it's drawn in a separate window layer on top of the `ModalBottomSheet`.
4. **Reduce top spacing/padding**: Reduce excess vertical padding around the top of the `SessionDetailHeader` to make the sheet layout more compact.

## Proposed Changes

### [TimelineScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/TimelineScreen.kt)

- Configure `ModalBottomSheet` with `rememberModalBottomSheetState(skipPartiallyExpanded = false)`.
- Use `LaunchedEffect` to sync `sheetState.currentValue` with the ViewModel's `isSheetExpanded` state, calling `expand()` and `partialExpand()` as needed.
- Define `expansionProgress` as an animated float state based on `state.isSheetExpanded`.
- Wrap the full-screen image overlay in a `Dialog` with full-screen properties (`usePlatformDefaultWidth = false`, `decorFitsSystemWindows = false`) so it renders on top of the bottom sheet.

### [SessionDetailSheet.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/SessionDetailSheet.kt)

- Receive `expansionProgress` as a regular Float (or float provider) to drive transitions.

### [SessionDetailComponents.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/components/SessionDetailComponents.kt)

- Reduce vertical/top padding on `SessionDetailHeader` to minimize spacing under the bottom sheet's drag handle.

## Verification Plan

- Verify the bottom sheet can be expanded to full screen and back down to peek height using drag gestures.
- Verify that expanding the sheet reveals consolidated screenshots/sessions (`ExpandedSessionContent`) and shows the total session count/durations.
- Verify that clicking on any screenshot thumbnail shows the image overlay on top of the bottom sheet.
