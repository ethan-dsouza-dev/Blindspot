package com.blindspot.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.GeoUtils
import kotlin.math.roundToInt

/**
 * "Midnight Aurora" search-radius control with tick marks at key distances.
 * The thumb tracks continuously for a smooth feel; [onRadiusChange] fires on every drag tick
 * while the actual network reload is debounced upstream (see PlacesViewModel.setRadius).
 *
 * @param radiusMeters current radius, clamped to [PlaceRepository.MIN_RADIUS_METERS] ..
 *   [PlaceRepository.MAX_RADIUS_METERS].
 * @param onRadiusChange called continuously as the user drags.
 */
@Composable
fun RadiusSlider(
    radiusMeters: Int,
    onRadiusChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minRadius = PlaceRepository.MIN_RADIUS_METERS.toFloat()
    val maxRadius = PlaceRepository.MAX_RADIUS_METERS.toFloat()

    // Tick mark positions: 500m, 1km, 2km, 5km — as fractions of the slider range.
    val tickFractions = remember(minRadius, maxRadius) {
        listOf(500f, 1000f, 2000f, 5000f)
            .filter { it in minRadius..maxRadius }
            .map { (it - minRadius) / (maxRadius - minRadius) }
    }
    val currentFraction = ((radiusMeters - minRadius) / (maxRadius - minRadius)).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Search radius",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraTokens.TextSecondary,
            )
            Text(
                text = GeoUtils.formatDistance(radiusMeters.toDouble()),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                ),
                color = AuroraTokens.AccentCyan,
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            tickFractions.forEach { fraction ->
                val x = size.width * fraction
                val reached = fraction <= currentFraction
                drawLine(
                    color = if (reached) AuroraTokens.AccentCyan else AuroraTokens.SurfaceBorder,
                    start = Offset(x, size.height * 0.2f),
                    end = Offset(x, size.height * 0.8f),
                    strokeWidth = 2f,
                )
            }
        }

        Slider(
            value = radiusMeters.toFloat(),
            onValueChange = { onRadiusChange(it.roundToInt()) },
            valueRange = minRadius..maxRadius,
            colors = SliderDefaults.colors(
                thumbColor = AuroraTokens.AccentCyan,
                activeTrackColor = AuroraTokens.AccentCyan,
                inactiveTrackColor = AuroraTokens.SurfaceElevated,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}