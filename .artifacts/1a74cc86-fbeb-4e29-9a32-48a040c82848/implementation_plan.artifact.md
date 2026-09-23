# Horizontal Pager Navigation for Highlight, Timeline, and Settings

Implement a horizontal swipeable container (`HorizontalPager`) for the core screens (`Highlight`, `Timeline`, and `Settings`) in the main app navigation, allowing users to navigate between them via swiping while keeping other destinations (Permissions, Auth, Paywall, CustomerCenter, FullScreenImage, etc.) in the stack. Also ensure the tutorial is fully compatible.

## User Review Required

> [!IMPORTANT]
> - **Swipe Navigation Order**: The horizontal pager will contain **Highlight** (index 0), **Timeline** (index 1), and **Settings** (index 2). Swiping left from Timeline opens Highlights, and swiping right opens Settings.
> - **Tutorial Integration**: The tutorial steps drive navigation between Timeline, Highlight, and Settings. We will coordinate `HorizontalPager` page selection with the TutorialViewModel effects so tutorial steps correctly scroll to the right page.

## Proposed Changes

### Navigation

#### [MODIFY] [AppNavigation.android.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/androidMain/kotlin/com/timeline/navigation/AppNavigation.android.kt)
- Introduce a `MainPagerScreen` containing a `HorizontalPager` with 3 pages: Highlight, Timeline, Settings.
- Connect Tutorial `NavigateToScreen` effect to page changes (`pagerState.animateScrollToPage(...)`).

#### [MODIFY] [AppNavigation.ios.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/iosMain/kotlin/com/timeline/navigation/AppNavigation.ios.kt)
- Update iOS navigation to support the same horizontal pager experience.

## Verification Plan

### Automated Tests
- Gradle build verification.
