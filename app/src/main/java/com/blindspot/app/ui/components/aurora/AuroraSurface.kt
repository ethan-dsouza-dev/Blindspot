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

enum class Elevation(val elevationDp: Dp) {
    Level1(4.dp),
    Level2(12.dp),
    Level3(24.dp)
}

@Composable
fun AuroraSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    elevation: Elevation = Elevation.Level1,
    content: @Composable () -> Unit
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
            .background(color = color, shape = shape)
    ) {
        content()
    }
}

@Composable
fun AuroraCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    content: @Composable () -> Unit
) = AuroraSurface(
    modifier = modifier,
    shape = shape,
    color = color,
    elevation = Elevation.Level1,
    content = content,
)

@Composable
fun AuroraFloating(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    content: @Composable () -> Unit
) = AuroraSurface(
    modifier = modifier,
    shape = shape,
    color = color,
    elevation = Elevation.Level2,
    content = content
)