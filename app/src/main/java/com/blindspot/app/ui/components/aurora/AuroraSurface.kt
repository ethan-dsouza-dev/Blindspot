package com.blindspot.app.ui.components.aurora

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Solid elevated "Midnight Aurora" card: opaque slate surface with a hairline border.
 * Deliberately not translucent — the Aurora system uses solid elevation instead of glass.
 */
@Composable
fun AuroraSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = AuroraTokens.SurfaceElevated,
    borderColor: Color = AuroraTokens.SurfaceBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = color, shape = shape)
            .border(borderWidth, borderColor, shape),
    ) {
        content()
    }
}
