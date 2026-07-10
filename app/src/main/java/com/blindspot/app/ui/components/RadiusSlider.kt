package com.blindspot.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.util.GeoUtils
import kotlin.math.roundToInt

/**
 * "Midnight Aurora" search-radius control. The thumb tracks continuously for a smooth feel;
 * [onRadiusChange] fires on every drag tick so the label stays live, while the actual network
 * reload is debounced upstream (see DiscoveryViewModel.setRadius).
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Search radius",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = GeoUtils.formatDistance(radiusMeters.toDouble()),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = radiusMeters.toFloat(),
            onValueChange = { onRadiusChange(it.roundToInt()) },
            valueRange = PlaceRepository.MIN_RADIUS_METERS.toFloat()..
                PlaceRepository.MAX_RADIUS_METERS.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
