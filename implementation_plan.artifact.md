# Redesign Authentication Screen

Redesign the authentication screen to match the provided mockup image.

## Proposed Changes

### [UI Components]

#### [MODIFY] [AuthComponents.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/components/AuthComponents.kt)
- Update `AuthHeader` to match the mockup's layout and spacing.
- Set the Hourglass icon color to a vibrant purple.
- Update `AuthAppIconsRow` to use `apple_logo` placeholder for the four app icons.
- Style the icon surfaces with a clean border and subtle shadow.
- Set the plus icon color to blue.
- Update `AuthForm` (Google Sign-In button) to match the mockup's styling: white background, rounded corners, and specific text styling.
- Update the footer text "Don't have an account? Sign in with Google" with correct coloring (gray for the prefix, purple for the link).

#### [MODIFY] [AuthScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/AuthScreen.kt)
- Ensure background is white as per mockup.
- Fine-tune spacing between components using `Spacer` and `weight`.

## Verification Plan

### Manual Verification
- Deploy the app to the emulator/device and visually compare the `AuthScreen` with the mockup image.
- Verify that the "Sign in with Google" button and the footer link are still functional.
- Ensure the app icons row and the main brand header are correctly aligned and spaced.
