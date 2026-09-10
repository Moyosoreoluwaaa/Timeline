# Implementation Plan - New AI Reasoning Highlight Screen Experience

We will design and implement a brand new, highly polished "AI Reasoning Highlight Screen" that groups user activity into time-of-day segments (Morning, Afternoon, Evening), displays deep executive reasoning from Gemini, features a smooth multi-word fading animation, and provides an engaging dual-card animated loading state.

## User Review Required

> [!IMPORTANT]
> This plan covers a complete redesign of the Highlight experience:
> 1. **Time-of-Day Categorization**: Grouping activity into Morning, Afternoon, and Evening filters right below the top bar.
> 2. **Animated Dual-Card Loading State**: Left and right thumbnail stacks with a dynamic animated transfer indicator between them when reasoning is loading.
> 3. **Word-by-Word Fading Typewriter Animation**: Smooth word-by-word streaming animation for the AI reasoning narrative.
> 4. **Automated Background Reasoning & Caching**: Robust background OCR/Reasoning persistence so returning users immediately see their generated highlights.

## Proposed Changes

### Component 1: Data & State Architecture (`shared/src/commonMain/kotlin/com/timeline/presentation/`)
- [MODIFY] [HighlightContract.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/HighlightContract.kt): Add time-of-day filter categories (`MORNING`, `AFTERNOON`, `EVENING`) and loading transfer states.
- [MODIFY] [HighlightViewModel.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/presentation/HighlightViewModel.kt): Automate cloud reasoning dispatch, caching, and time-of-day segmentation.

### Component 2: New Highlight UI & Animations (`shared/src/commonMain/kotlin/com/timeline/ui/`)
- [MODIFY] [HighlightScreen.kt](file:///C:/Users/USER/AndroidStudioProjects/Timeline/shared/src/commonMain/kotlin/com/timeline/ui/HighlightScreen.kt):
  - Implement top-bar time-of-day filter chips.
  - Implement dual-card animated loading state (Left & Right stacks with animated indicator).
  - Implement word-by-word fading typewriter animation for AI reasoning.
  - Structure clean executive narrative cards grouped by Morning, Afternoon, and Evening.

## Verification Plan

### Automated Tests
- Build verification via Gradle (`:androidApp:assembleDebug`)

### Manual Verification
- Deploy to device/emulator, navigate to the Highlight screen.
- Verify the dual-card animated loading state when reasoning is processing.
- Verify time-of-day segments (Morning, Afternoon, Evening) and word-by-word fading text animation.
