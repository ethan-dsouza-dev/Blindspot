package com.blindspot.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Design tokens — single source of truth for color. Every property here is backed by
 * [currentPalette], a Compose [mutableStateOf]. Because Compose's snapshot system tracks state
 * reads regardless of whether they happen through a plain property getter, every existing
 * `AuroraTokens.X` reference across the app automatically recomposes when [setPalette] changes
 * the active theme — no call site needs to change.
 */
object AuroraTokens {

    private var currentPalette by mutableStateOf(AuroraPalette)

    fun setPalette(palette: ColorPalette) {
        currentPalette = palette
    }

    val BaseDeep: Color get() = currentPalette.baseDeep
    val BaseSlate: Color get() = currentPalette.baseSlate

    val SurfaceElevated: Color get() = currentPalette.surfaceElevated
    val SurfaceBorder: Color get() = currentPalette.surfaceBorder

    val AccentCyan: Color get() = currentPalette.accentCyan
    val AccentTeal: Color get() = currentPalette.accentTeal

    val AccentCyanSubtle: Color get() = AccentCyan.copy(alpha = 0.05f)
    val AccentCyanGlow: Color get() = AccentCyan.copy(alpha = 0.15f)
    val AccentCyanPress: Color get() = AccentCyan.copy(alpha = 0.20f)

    val OnAccent: Color get() = BaseDeep

    val TextPrimary: Color get() = currentPalette.textPrimaryBase
    val TextSecondary: Color get() = TextPrimary.copy(alpha = 0.70f)
    val TextTertiary: Color get() = TextPrimary.copy(alpha = 0.50f)

    val Positive: Color get() = currentPalette.positive
    val Negative: Color get() = currentPalette.negative

    val RatingStar: Color get() = currentPalette.ratingStar

    val ShadowTint: Color get() = BaseDeep.copy(alpha = 0.40f)
    val ShadowLevel1: Color get() = ShadowTint.copy(alpha = 0.20f)
    val ShadowLevel2: Color get() = ShadowTint.copy(alpha = 0.25f)
    val ShadowLevel3: Color get() = ShadowTint.copy(alpha = 0.30f)

    val CompassDialFill: Color get() = currentPalette.compassDialFill
    val CompassDialStroke: Color get() = currentPalette.compassDialStroke
    val CompassDialInnerStroke: Color get() = currentPalette.compassDialInnerStroke
    val CompassTickMajor: Color get() = currentPalette.compassTickMajor
    val CompassTickMinor: Color get() = currentPalette.compassTickMinor
    val CompassNeedleTail: Color get() = currentPalette.compassNeedleTail
    val CompassHub: Color get() = currentPalette.compassHub
    val CompassHubInner: Color get() = AccentCyan
}