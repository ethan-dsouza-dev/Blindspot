package com.blindspot.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Midnight Aurora" design tokens — the single source of truth for color across the app.
 * Deep slate base, one cyan accent used sparingly (needle, active states, CTAs), and solid
 * elevated surfaces (no translucent glass).
 */
object AuroraTokens {

    // Base backgrounds.
    val BaseDeep = Color(0xFF0B0F14)
    val BaseSlate = Color(0xFF11161D)

    // Elevated surfaces.
    val SurfaceElevated = Color(0xFF1A2129)
    val SurfaceBorder = Color(0xFF2A323C)

    // Accent (used sparingly: needle, active states, CTAs).
    val AccentCyan = Color(0xFF22D3EE)
    val AccentTeal = Color(0xFF2DD4BF)

    // Content rendered on top of an accent-filled surface (e.g. primary button labels).
    val OnAccent = BaseDeep

    // Text.
    val TextPrimary = Color(0xFFEDF1F5)
    val TextSecondary = Color(0xFF8B96A5)

    // Semantic statuses.
    val Positive = Color(0xFF4ADE80)
    val Negative = Color(0xFFF87171)

    // Ratings.
    val RatingStar = Color(0xFFF5C044)

    // Compass-specific.
    val CompassDialFill = Color(0xFF141A21)
    val CompassDialStroke = Color(0xFF2A323C)
    val CompassDialInnerStroke = Color(0xFF212932)
    val CompassTickMajor = Color(0xFF5A6675)
    val CompassTickMinor = Color(0xFF2E3742)
    val CompassNeedleTail = Color(0x40EDF1F5)
    val CompassHub = Color(0xFFEDF1F5)
    val CompassHubInner = AccentCyan
}
