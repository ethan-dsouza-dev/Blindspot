package com.blindspot.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Midnight Aurora" design tokens — single source of truth for color.
 * Deep slate base, cyan accent used sparingly (needle, active states, CTAs).
 * Solid elevated surfaces with tinted shadows (no translucent glass).
 */
object AuroraTokens {

    // Base backgrounds (60%)
    val BaseDeep = Color(0xFF0B0F14)
    val BaseSlate = Color(0xFF11161D)

    // Elevated surfaces
    val SurfaceElevated = Color(0xFF1A2129)
    val SurfaceBorder = Color(0xFF2A323C)

    // Accent — used sparingly (10%): needle, active states, primary CTAs
    val AccentCyan = Color(0xFF22D3EE)
    val AccentTeal = Color(0xFF2DD4BF)

    // Accent variations for hierarchy
    val AccentCyanSubtle = AccentCyan.copy(alpha = 0.05f)   // Secondary button bg, card highlights
    val AccentCyanGlow = AccentCyan.copy(alpha = 0.15f)     // Lock-on glow, focus rings
    val AccentCyanPress = AccentCyan.copy(alpha = 0.20f)    // Press states

    // Content on accent-filled surfaces
    val OnAccent = BaseDeep

    // Text hierarchy (30%) — opacity-based on TextPrimary
    val TextPrimary = Color(0xFFEDF1F5)      // 100% — headings, primary content
    val TextSecondary = TextPrimary.copy(alpha = 0.70f)  // 70% — body, metadata
    val TextTertiary = TextPrimary.copy(alpha = 0.50f)   // 50% — disabled, hints, timestamps

    // Semantic statuses
    val Positive = Color(0xFF4ADE80)
    val Negative = Color(0xFFF87171)

    // Ratings
    val RatingStar = Color(0xFFF5C044)

    // Shadow system — tinted to background, never pure gray/black
    val ShadowTint = BaseDeep.copy(alpha = 0.40f)
    val ShadowLevel1 = ShadowTint.copy(alpha = 0.20f)  // 0 1px 3px
    val ShadowLevel2 = ShadowTint.copy(alpha = 0.25f)  // 0 4px 12px
    val ShadowLevel3 = ShadowTint.copy(alpha = 0.30f)  // 0 8px 24px

    // Compass-specific
    val CompassDialFill = Color(0xFF141A21)
    val CompassDialStroke = Color(0xFF2A323C)
    val CompassDialInnerStroke = Color(0xFF212932)
    val CompassTickMajor = Color(0xFF5A6675)
    val CompassTickMinor = Color(0xFF2E3742)
    val CompassNeedleTail = Color(0x40EDF1F5)
    val CompassHub = Color(0xFFEDF1F5)
    val CompassHubInner = AccentCyan
}