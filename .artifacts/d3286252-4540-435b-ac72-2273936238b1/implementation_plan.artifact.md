# Implementation Plan: Onboarding, Splash, Tutorial, and Timeline Enhancements

This plan addresses all specified bug fixes and feature enhancements for the Timeline app.

## User Review Required

> [!IMPORTANT]
> - **Fresh Launch Navigation**: On fresh launch, the app will navigate directly to the `Opening` screen (`Permission` route).
> - **Custom Splash Screen**: Custom splash screen with `ic_launcher_foreground` and light sweep animation.
> - **Free vs Pro Tutorial & Paywall**: Free tutorial has TimelineScreen tutorial only; Pro tutorial has all tutorials ending in Paywall (dismissible back to Free, or Pro upon payment).
> - **Highlight Tutorial Skip**: Clicking summary card skips loading state for the sake of the tutorial.
> - **Edge Gestures**: Spotlight edges of the screen with animated arrows for drag left/right navigation.
> - **Preference Saving**: Persist tutorial completion state immediately on skip or complete to prevent looping.
> - **Shimmer Effect**: Add shimmer effect using `TimelineEntry` for 2 seconds after tutorial completion before showing empty state.

## Proposed Changes

### [Navigation & Splash]
- Custom splash screen with light-sweep animation in `AppNavigation`.
- Correct initial navigation state.

### [Tutorial & Plan Selection]
- Free vs Pro tier branching in `PermissionComponents` & `PermissionViewModel`.
- Immediate preference saving in `TutorialViewModel`.
- Edge gesture tutorials and summary card click handling in `TutorialShowcaseOverlay` & `TutorialViewModel`.

### [Timeline & Shimmer]
- 2-second shimmer effect using `TimelineEntry` placeholders in `TimelineScreen`.

## Verification Plan

### Automated Tests
- Build project successfully.

### Manual Verification
- Verify splash screen, onboarding, tutorial branching, edge gestures, and shimmer effect.
