package com.timeline.domain.model

import com.timeline.presentation.TimeOfDayFilter

/**
 * Slide kinds used in the story reel.
 */
enum class ReelSlideKind {
    RECAP,
    OUTLIER,
    STAT
}

/**
 * A simulated (or real) app screenshot thumbnail displayed inside a reel slide.
 */
data class ReelSlideScreenshot(
    val appName: String,
    val iconKey: String,
    val colorHex: Long,
    val timestamp: String,
    val isHighlighted: Boolean = false
)

/**
 * A single slide in the interactive story reel.
 */
data class ReelSlide(
    val id: String,
    val kind: ReelSlideKind,
    val keyLabel: String,
    val headline: String,
    val badgeTag: String? = null,
    val stat: String? = null,
    val sub: String? = null,
    val shots: List<ReelSlideScreenshot> = emptyList(),
    val bgColorHex: Long,
    val fgColorHex: Long,
    val accentColorHex: Long,
    val isCrossDay: Boolean = false
)

/**
 * A grouped time-of-day segment used in the reel highlight card list.
 */
data class ReelSegment(
    val filter: TimeOfDayFilter,
    val title: String,
    val narrative: String,
    val paragraphs: List<String> = emptyList(),
    val accentColor: Long,
    val isUpcoming: Boolean = false,
    val slides: List<ReelSlide> = emptyList()
)
