package com.blindspot.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Every color the app currently defines, extracted from the old static AuroraTokens so a
 * second palette (Dusk) can provide its own values for the same set of tokens. */
data class ColorPalette(
    val baseDeep: Color,
    val baseSlate: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val accentCyan: Color,
    val accentTeal: Color,
    val textPrimaryBase: Color,
    val positive: Color,
    val negative: Color,
    val ratingStar: Color,
    val compassDialFill: Color,
    val compassDialStroke: Color,
    val compassDialInnerStroke: Color,
    val compassTickMajor: Color,
    val compassTickMinor: Color,
    val compassNeedleTail: Color,
    val compassHub: Color,
)

enum class AppTheme { AURORA, DUSK }

val AuroraPalette = ColorPalette(
    baseDeep = Color(0xFF0B0F14),
    baseSlate = Color(0xFF11161D),
    surfaceElevated = Color(0xFF1A2129),
    surfaceBorder = Color(0xFF2A323C),
    accentCyan = Color(0xFF22D3EE),
    accentTeal = Color(0xFF2DD4BF),
    textPrimaryBase = Color(0xFFEDF1F5),
    positive = Color(0xFF4ADE80),
    negative = Color(0xFFF87171),
    ratingStar = Color(0xFFF5C044),
    compassDialFill = Color(0xFF141A21),
    compassDialStroke = Color(0xFF2A323C),
    compassDialInnerStroke = Color(0xFF212932),
    compassTickMajor = Color(0xFF5A6675),
    compassTickMinor = Color(0xFF2E3742),
    compassNeedleTail = Color(0x40EDF1F5),
    compassHub = Color(0xFFEDF1F5),
)

/** Warm twilight companion to Aurora: deep indigo/plum base instead of slate, amber accent
 * instead of cyan. Still a dark theme — this app is dark-only by design (night-out product). */
val DuskPalette = ColorPalette(
    baseDeep = Color(0xFF120B14),
    baseSlate = Color(0xFF1A1220),
    surfaceElevated = Color(0xFF261A2E),
    surfaceBorder = Color(0xFF3A2A44),
    accentCyan = Color(0xFFF5A855),
    accentTeal = Color(0xFFE8895A),
    textPrimaryBase = Color(0xFFF5EDF1),
    positive = Color(0xFF4ADE80),
    negative = Color(0xFFF87171),
    ratingStar = Color(0xFFF5C044),
    compassDialFill = Color(0xFF1E1424),
    compassDialStroke = Color(0xFF3A2A44),
    compassDialInnerStroke = Color(0xFF2C2035),
    compassTickMajor = Color(0xFF6E5A78),
    compassTickMinor = Color(0xFF3D2E47),
    compassNeedleTail = Color(0x40F5EDF1),
    compassHub = Color(0xFFF5EDF1),
)