# Free-Access & Future Paywall Gating Implementation Plan

This implementation plan outlines the steps to make **all premium features 100% free and accessible** for the current release iteration, while documenting the exact changes required in the next iteration to re-enable the paywall and lock features behind RevenueCat (P1 / Pro entitlements).

## User Review Required

> [!IMPORTANT]
> **Current Version Goal**: Every paywalled/Pro feature (such as advanced insights, longer data retention, app exclusions, etc.) is made fully accessible to all users without requiring a purchase or active subscription.
>
> **Future Version Goal**: A clear blueprint is provided below so you can easily revert or flip the flags back to enforce the paywall when submitting to the Google Play Store.

## Open Questions

- None. The strategy is to override `isPro` to always emit `true` (or bypass entitlement checks) in the current iteration, and provide a revertible plan for production release.

---

## Proposed Changes

### Subscription & Feature Gating / Feature Flag

To make everything free right now and easy to toggle back for the Play Store release, we will introduce a clean feature flag (e.g., `isFreeAccessEnabled = true`) inside `RevenueCatSubscriptionManager` or a dedicated config object.

#### [MODIFY] [RevenueCatSubscriptionManager.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/domain/RevenueCatSubscriptionManager.kt)
- Introduce a feature flag boolean (e.g., `private val isFreeAccessEnabled = true`).
- When `isFreeAccessEnabled` is `true`:
  - `_isPro` is forced to `true`.
  - `checkEntitlement` returns `true` for any entitlement ID.
- When you are ready for the Play Store release, simply flip `isFreeAccessEnabled` to `false`, and the app will fully respect real RevenueCat subscriptions and paywalls.

---

## Future Re-Enabling Plan (For Play Store Release)

When you are ready to release to the Google Play Store with the paywall enabled, follow these steps:

1. **Restore `RevenueCatSubscriptionManager.kt`**:
   - Revert `_isPro` initialization back to `false` (or base it strictly on RevenueCat customer info entitlements).
   - Revert `checkEntitlement` to check `_customerInfo.value?.entitlements?.active?.containsKey(entitlementId) == true`.

2. **Verify Paywall Triggers**:
   - Ensure `onNavigateToPaywall` is correctly wired when users tap on locked features or settings banners.
   - Test RevenueCat offerings and purchases in Google Play Console internal testing / sandbox mode.

3. **Check Trial / Subscription Status Flows**:
   - Ensure `SettingsViewModel`, `HighlightViewModel`, and `PermissionScreen` correctly reflect the real subscription state (`isPro` from RevenueCat).
