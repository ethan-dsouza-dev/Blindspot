package com.blindspot.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.GlassBorder
import com.blindspot.app.ui.theme.GlassHighlight
import com.blindspot.app.ui.theme.GlassTint

/**
 * Reusable frosted-glass container. Renders a translucent tinted surface with a soft top
 * highlight and a hairline border for a Gemini-style "glassmorphism" look.
 *
 * Note on blur: true backdrop blur requires API 31+ (`Modifier.blur` / RenderEffect). The
 * translucent gradient + border below reads as glassy on all supported API levels (min 29);
 * a real blur can be layered on later for 31+ without changing this component's API.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color = GlassTint,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassHighlight.copy(alpha = 0.18f),
                        tint,
                        tint.copy(alpha = (tint.alpha * 0.7f).coerceIn(0f, 1f)),
                    ),
                ),
                shape = shape,
            )
            .border(borderWidth, borderColor, shape),
    ) {
        content()
    }
}
