package com.timeline.ui.components

internal val CardHandSpec = FanSpec(
    widthFraction = 0.66f,
    shots = listOf(
        ShotSpec(-17f, (-30).m, (-6).m, accentAlpha = 0.35f),
        ShotSpec(15f, 30.m, (-2).m, accentAlpha = null),
        ShotSpec(-5f, (-8).m, (-12).m, accentAlpha = 0.20f),
    )
)
