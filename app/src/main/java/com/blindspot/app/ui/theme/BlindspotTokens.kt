package com.blindspot.app.ui.theme

import androidx.compose.ui.graphics.Color


object BlindspotTokens {

    // Primary brand colors
    val Primary = GeminiBlue
    val PrimaryAccent = GeminiViolet
    val PrimarySecondary = GeminiPink
    val PrimaryTertiary = GeminiTeal

    // Background colors
    val BackgroundPrimary = BackgroundTop
    val BackgroundSecondary = BackgroundMid
    val BackgroundTertiary = BackgroundBottom

    // Surface colors
    val SurfacePrimary = GlassTint
    val SurfaceBorder = GlassBorder
    val SurfaceHighlight = GlassHighlight

    // Text colors
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.7f)
    val TextTertiary = Color.White.copy(alpha = 0.85f)
    val TextQuaternary = Color.White.copy(alpha = 0.8f)

    // UI element colors
    val IconPrimary = Color.White
    val IconSecondary = Color.White.copy(alpha = 0.7f)
    val IconAccent = GeminiBlue

    // Compass-specific colors
    val CompassDialFill = Color.White.copy(alpha = 0.06f)
    val CompassDialStroke = Color.White.copy(alpha = 0.18f)
    val CompassDialInnerStroke = Color.White.copy(alpha = 0.10f)
    val CompassTickMajor = Color.White.copy(alpha = 0.45f)
    val CompassTickMinor = Color.White.copy(alpha = 0.18f)
    val CompassNeedleTail = Color.White.copy(alpha = 0.25f)
    val CompassHub = Color.White
    val CompassHubInner = GeminiViolet

    // Rating and metadata colors
    val RatingStar = Color(0xFFFFC93C)
    val RatingText = Color.White.copy(alpha = 0.85f)
    val PriceLevel = Color.White.copy(alpha = 0.7f)

    // Sheet and overlay colors
    val SheetBackground = BackgroundMid.copy(alpha = 0.96f)

    // Navigation colors
    val NavItemActive = GeminiBlue
    val NavItemInactive = Color.White.copy(alpha = 0.6f)
    val NavBarTint = Color.White.copy(alpha = 0.1f)
}
