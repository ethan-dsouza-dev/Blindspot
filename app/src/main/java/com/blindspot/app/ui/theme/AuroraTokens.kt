package com.blindspot.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Midnight Aurora" design tokens: deep slate base, a single cyan→teal accent, and solid
 * elevated surfaces (no translucent glass). Currently applied only to the Discovery screen
 * while the design system is being evaluated.
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

    // Text.
    val TextPrimary = Color(0xFFEDF1F5)
    val TextSecondary = Color(0xFF8B96A5)

    // Ratings / warnings.
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
