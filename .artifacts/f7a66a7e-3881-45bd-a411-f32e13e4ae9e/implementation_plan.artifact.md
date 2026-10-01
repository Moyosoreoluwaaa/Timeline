# Implementation Plan - Enhancing Onboarding Flow with Interactive Design Choices

This plan outlines the design and implementation strategy for updating and enriching the onboarding flow in Timeline to match the multi-step interactive design showcased by the user.

## User Review Required

> [!IMPORTANT]
> - We will extend `OnboardingStep` enum and update `PermissionCardStack` or introduce additional onboarding steps/cards to incorporate rich value propositions, interactive feature previews, and permission flows matching the requested design.
> - Ensure seamless navigation across all onboarding steps with step indicators, animations, and clean state persistence.

## Open Questions

- None at this stage.

## Proposed Changes

### Shared Module (`shared`)

#### [MODIFY] [PermissionContract.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/PermissionContract.kt)
- Expand `OnboardingStep` to represent the desired onboarding sequence (e.g. `Opening`, `Welcome`, `ValueProposition`, `PermissionCardStack`, `FeatureHighlight`, etc. or refine steps as needed).

#### [MODIFY] [PermissionComponents.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/components/PermissionComponents.kt)
- Update onboarding step contents and card stack animations/layouts to match the requested design and polished UI components.

#### [MODIFY] [PermissionViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/PermissionViewModel.kt)
- Handle navigation between the new onboarding steps correctly and persist step progress.

## Verification Plan

### Automated Tests
- Build project using gradle build (`gradle_build("androidApp:assembleDebug")`).

### Manual Verification
- Deploy app to emulator/device, verify the entire onboarding flow step by step.
