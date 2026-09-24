# Implementation Plan: Adopting New Design System

This plan outlines the steps to integrate the new design system (based on the AllTrails teardown) into the Timeline app, while substituting the brand color with our own primary color.

## User Review Required

> [!IMPORTANT]
> I need you to provide your **primary color hex code** to replace the teardown's `#264311`.

> [!WARNING]
> This will involve significant changes to the base `Theme.kt` and potentially widespread refactoring of UI components if they are not currently using a centralized design system.

## Open Questions

- What is the primary color hex code for your brand?
- Are you currently using `MaterialTheme` in your project for colors and shapes?

## Proposed Changes

### [Theme Infrastructure]

#### [MODIFY] [Theme.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/androidApp/src/main/java/com/timeline/ui/theme/Theme.kt)
- Define new `Palette`, `Space`, `Shapes`, and `Type` objects using the teardown's token logic, substituting your primary color.
- Integrate these into the `MaterialTheme` configuration.

### [Component Migration]

#### [MODIFY] [Components.kt (or equivalent)]
- Refactor existing Buttons, Cards, and Search bars to use the new pill shapes (`radius-pill`), spacing (`16.dp`, `24.dp`), and color roles (`Palette.Ink`, `Palette.FillNeutral`, etc.).

## Verification Plan

### Automated Tests
- I will use `render_compose_preview` to verify the new design system components.

### Manual Verification
- Deploy the app to the emulator/device to check the "feel" (spacing, radii, colors) of key screens like the main dashboard and detail views.
