package com.blindspot.app.ui.components.aurora

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Solid elevated "Midnight Aurora" surface: opaque slate card with a hairline border and a
 * tinted soft shadow. Three elevation levels replace the old single flat-card style.
 */
enum class Elevation(val elevationDp: Dp) {
    Level1(4.dp),
    Level2(12.dp),
    Level3(24.dp),
}

@Composable
fun AuroraSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    borderColor: Color = AuroraTokens.SurfaceBorder,
    borderWidth: Dp = 1.dp,
    elevation: Elevation = Elevation.Level1,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation.elevationDp,
                shape = shape,
                clip = false,
                ambientColor = AuroraTokens.ShadowTint,
                spotColor = AuroraTokens.ShadowTint,
            )
            .clip(shape)
            .background(color = color, shape = shape)
            .border(borderWidth, borderColor, shape),
    ) {
        content()
    }
}

/** Convenience: Level 1 card (default for static content). */
@Composable
fun AuroraCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    borderColor: Color = AuroraTokens.SurfaceBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) = AuroraSurface(
    modifier = modifier,
    shape = shape,
    color = color,
    borderColor = borderColor,
    borderWidth = borderWidth,
    elevation = Elevation.Level1,
    content = content,
)

/** Convenience: Level 2 floating element (nav pill, dropdowns, recenter button). */
@Composable
fun AuroraFloating(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    borderColor: Color = AuroraTokens.SurfaceBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) = AuroraSurface(
    modifier = modifier,
    shape = shape,
    color = color,
    borderColor = borderColor,
    borderWidth = borderWidth,
    elevation = Elevation.Level2,
    content = content,
)