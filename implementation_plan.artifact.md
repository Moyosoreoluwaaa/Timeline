# Redesign App Navigation and Implement New Insights Flow

This plan outlines the redesign of the Timeline app's navigation and the implementation of four new primary screens: Insights, Apps, Trends, and Patterns, as shown in the provided mockups.

## User Review Required

> [!IMPORTANT]
> This plan introduces a major navigation overhaul, moving from a stack-based/hidden-detail navigation to a 4-tab Bottom Navigation bar. This contradicts the current "Exactly 2 screens" rule in the Product Definition, but aligns with the provided mockups.

> [!IMPORTANT]
> I will consolidate the "Primary Color" (Vibrant Purple `0xFF6C5CE7`) and other brand colors into a central utility-like location as requested, specifically `shared/src/commonMain/kotlin/com/timeline/ui/theme/Color.kt` (acting as the requested `/utils`).

## Proposed Changes

### [Theme & Utilities]

#### [MODIFY] [Color.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/theme/Color.kt)
- Add the vibrant primary color `val VibrantPurple = Color(0xFF6C5CE7)`.
- Add secondary colors for charts and app icons matching the mockup (e.g., specific shades for YouTube, Instagram, etc.).
- Update `AppColors` to include these as brand-standard values.

#### [MODIFY] [Theme.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/theme/Theme.kt)
- Update `LightColorScheme` and `DarkColorScheme` to use `VibrantPurple` as the `primary` color.
- Ensure `surfaceVariant` and `background` colors align with the clean, white/light-gray look of the mockups.

### [Navigation & Main Flow]

#### [NEW] [MainScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/MainScreen.kt)
- Create a new host screen with a `Scaffold` and `NavigationBar`.
- Define the 4 navigation items: Insights, Apps, Trends, Patterns.
- Handle state for the selected tab.

#### [MODIFY] [Route.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/navigation/Route.kt)
- Update routes to include the new 4 tabs (replacing or augmenting `Metrics` and `Timeline`).

#### [MODIFY] [AppNavigation.android.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/navigation/AppNavigation.android.kt)
- Update the navigation graph to point to `MainScreen` after login/permissions.

### [Feature Screens]

#### [MODIFY] [MetricsScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/MetricsScreen.kt)
- Rename or refactor into `InsightsScreen` to match the first mockup.
- Implement the "Total usage" header, weekly bar chart, and the donut chart for usage distribution.

#### [NEW] [AppsScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/AppsScreen.kt)
- Implement the "Apps" list with colorful icons and progress bars.
- Include the "All Apps" section with secondary app list.

#### [NEW] [TrendsScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/TrendsScreen.kt)
- Implement the "Trends" line chart and the "Highest/Lowest" summary cards.

#### [NEW] [PatternsScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/PatternsScreen.kt)
- Implement the "Patterns" cards (Peak at night, Longest session, etc.) and the "More Patterns" list.

---

## Verification Plan

### Automated Tests
- No new automated tests are planned for this UI-heavy redesign, but existing navigation tests will be updated to reflect the new `MainScreen` entry point.

### Manual Verification
- Deploy to an Android device/emulator.
- Verify the Bottom Navigation bar correctly switches between the 4 new tabs.
- Visually inspect each screen (Insights, Apps, Trends, Patterns) against the provided mockups for:
  - Color accuracy (Vibrant Purple `0xFF6C5CE7`).
  - Layout and spacing (Rounded corners, card styles).
  - Chart rendering (Bar chart, Line chart, Donut chart).
