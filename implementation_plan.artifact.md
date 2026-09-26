# Implementation Plan: Onboarding, Splash, Tutorial, and Timeline Enhancements

This plan addresses all 8 specified bug fixes and feature enhancements for the Timeline app.

## User Review Required

> [!IMPORTANT]
> - **Fresh Launch Navigation**: On fresh launch, the app will navigate directly to the `Opening` screen (`Permission` route) instead of `welcome`.
> - **Custom Splash Screen**: We will add a branded native/Compose splash experience utilizing `ic_launcher_foreground` with a light-sweep animation, transitioning from Splash -> Opening -> Full Onboarding -> Tutorial -> Timeline.
> - **Free vs Pro Tutorial & Paywall**: Free users receive a streamlined tutorial ending in the Timeline screen. Pro users experience the full tutorial ending in the Paywall (which can be dismissed back to Free, or proceed to payment).
> - **Highlight Tutorial Skip**: Clicking summary cards skips loading states during the tutorial.
> - **Gesture Tutorial**: Highlighted screen edges with animated arrows for dragging left (to Timeline) and right (to Settings).
> - **Preference Saving**: Persist tutorial completion state immediately upon completion/skip to prevent looping.
> - **Shimmer Effect**: Add shimmer effect using `TimelineEntry` for 2 seconds after tutorial completion before showing the empty state.

## Proposed Changes

### [Navigation & Splash]

#### [MODIFY] [AppNavigation.android.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/navigation/AppNavigation.android.kt)
- Fix initial navigation check to start at `Opening` (`Permission` route) when permissions are not completed, ensuring correct startup sequence.
- Integrate custom splash screen with light-sweep animation before onboarding.

#### [MODIFY] [PermissionViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/PermissionViewModel.kt)
- Ensure initial onboarding step is `Opening`.

### [Tutorial & Plan Selection]

#### [MODIFY] [PermissionComponents.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/components/PermissionComponents.kt)
- Implement Free vs Pro tier distinction in `PlanSelectionStep`: Free user skips Pro paywall and enters Timeline tutorial directly; Pro user goes through full tutorial ending in Paywall.

#### [MODIFY] [TutorialViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/tutorial/TutorialViewModel.kt)
- Fix tutorial completion persistence immediately upon skip or complete so it never loops.
- Update tutorial steps to include highlight summary card click (skipping loading state) and edge drag gestures (left to Timeline, right/left to Settings).

#### [MODIFY] [TutorialScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/tutorial/TutorialScreen.kt)
- Add animated edge arrows and spotlight overlay handlers for horizontal pager / edge gestures.

### [Timeline & Shimmer]

#### [MODIFY] [TimelineScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/TimelineScreen.kt)
- Implement 2-second shimmer effect using `TimelineEntry` placeholders after tutorial completion/dismissal before showing empty state.

---

## Verification Plan

### Automated Tests
- Build and run gradle checks (`gradle_build("app:assembleDebug")`).

### Manual Verification
- Deploy to emulator/device, verify splash screen with light sweep, onboarding flow, Free/Pro tutorial branching, edge gesture spotlights, and 2-second shimmer empty state.
