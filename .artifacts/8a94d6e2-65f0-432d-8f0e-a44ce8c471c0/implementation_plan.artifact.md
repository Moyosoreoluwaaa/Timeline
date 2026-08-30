# RevenueCat Integration Production Upgrade

Upgrade the current RevenueCat implementation in the Timeline KMP project to match the production-level quality and patterns found in the RevenueCat sample app.

## User Review Required

> [!IMPORTANT]
> This upgrade will change how RevenueCat is configured and managed. Ensure you have your RevenueCat API keys and Entitlement IDs ready for the new configuration structure.

## Proposed Changes

### Configuration & Initialization

I will consolidate RevenueCat configuration and improve the initialization process to be more robust and multiplatform-friendly.

#### [MODIFY] [RevenueCatConfig.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/RevenueCatConfig.kt)
- Update to use a more structured configuration object.
- Add support for Entitlement IDs.

#### [MODIFY] [AndroidSubscriptionInitializer.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/domain/AndroidSubscriptionInitializer.kt)
- Use `PurchasesConfiguration.Builder` for better Android-specific configuration (logging, etc.).

### Domain Logic

I will improve the `RevenueCatSubscriptionManager` to handle purchases, restores, and updates more gracefully, following the sample app's `UserViewModel` patterns.

#### [MODIFY] [SubscriptionManager.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/SubscriptionManager.kt)
- Add more granular state tracking (e.g., `isPurchasing`, `errors`).

#### [MODIFY] [RevenueCatSubscriptionManager.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/RevenueCatSubscriptionManager.kt)
- Use built-in KMP coroutine support for RevenueCat.
- Implement better error handling, specifically for user cancellations.
- Add logging using Kermit (matching the sample app's usage of Log).
- Ensure `customerInfo` updates are correctly propagated via the delegate.

## Verification Plan

### Automated Tests
- Build the shared module to ensure KMP compilation passes.
- I will attempt to run `gradle_build(":shared:assembleDebug")` (or equivalent for KMP) to verify syntax and dependency alignment.

### Manual Verification
- The user should verify the integration on a physical device/simulator to ensure the RevenueCat purchase flow is triggered correctly and entitlement status is updated.
