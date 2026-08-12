package com.blindspot.app.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.theme.AuroraTokens
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Trending-now pins for the map: smaller cyan dots with dark rings, drawn below the destination
 * pin so the destination stays the visual focal point. Tapping a pin invokes [onPinClick]; the
 * tapped place id is carried through the feature properties.
 *
 * Must be invoked inside a [org.maplibre.compose.map.MaplibreMap] content block.
 */
@Composable
fun TrendingPinsLayer(
    places: List<Place>,
    onPinClick: (Place) -> Unit,
) {
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            FeatureCollection(
                places.map { place ->
                    Feature(
                        Point(Position(place.longitude, place.latitude)),
                        JsonObject(mapOf("id" to JsonPrimitive(place.id))),
                    )
                },
            ),
        ),
    )
    CircleLayer(
        id = "trending-ring",
        source = source,
        color = const(AuroraTokens.BaseDeep),
        radius = const(8.dp),
    )
    CircleLayer(
        id = "trending-pin",
        source = source,
        color = const(AuroraTokens.AccentCyan),
        radius = const(5.dp),
        onClick = { features ->
            val clickedId = features.firstOrNull()
                ?.properties
                ?.get("id")
                ?.jsonPrimitive
                ?.contentOrNull
            val place = clickedId?.let { id -> places.find { it.id == id } }
            if (place != null) {
                onPinClick(place)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
    )
}