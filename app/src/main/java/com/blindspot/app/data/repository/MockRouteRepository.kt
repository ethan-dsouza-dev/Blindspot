package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Route
import com.blindspot.app.data.model.RoutePoint
import com.blindspot.app.data.remote.RouteDto
import com.blindspot.app.data.remote.toDomain
import com.blindspot.app.util.GeoUtils
import com.blindspot.app.util.PolylineCodec
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Temporary in-memory [RouteRepository] used until the backend routing endpoint exists.
 *
 * Builds a plausible multi-point path between origin and destination, encodes it as a polyline
 * (mimicking the payload the Geoapify-backed backend would return), then decodes it through the
 * same [PolylineCodec] path used in production. This lets the Maps tab render a real route line
 * end-to-end without a live network.
 */
class MockRouteRepository : RouteRepository {

    override suspend fun getRoute(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
        mode: String,
    ): Result<Route> = runCatching {
        // Simulate network latency.
        delay(500)

        val path = buildPath(fromLatitude, fromLongitude, toLatitude, toLongitude)
        val distance = pathDistanceMeters(path)

        val dto = RouteDto(
            polyline = PolylineCodec.encode(path),
            precision = PolylineCodec.DEFAULT_PRECISION,
            distanceMeters = distance,
            durationSeconds = distance / walkingSpeed(mode),
        )
        dto.toDomain()
    }

    /**
     * Interpolates [SEGMENTS] points from origin to destination, nudging the midsection sideways
     * so the line curves like a street route rather than a straight beeline.
     */
    private fun buildPath(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
    ): List<RoutePoint> {
        val dLat = toLat - fromLat
        val dLng = toLng - fromLng
        return (0..SEGMENTS).map { step ->
            val t = step.toDouble() / SEGMENTS
            // Perpendicular offset that peaks at the midpoint and vanishes at both ends.
            val bow = sin(t * Math.PI) * CURVE_FACTOR
            RoutePoint(
                latitude = fromLat + dLat * t - dLng * bow,
                longitude = fromLng + dLng * t + dLat * bow,
            )
        }
    }

    private fun pathDistanceMeters(path: List<RoutePoint>): Double =
        path.zipWithNext().sumOf { (a, b) ->
            GeoUtils.distanceMeters(a.latitude, a.longitude, b.latitude, b.longitude)
        }

    private fun walkingSpeed(mode: String): Double = when (mode) {
        "drive" -> 11.0 // ~40 km/h
        "bicycle" -> 4.5
        else -> 1.4 // walking, ~5 km/h
    }

    private companion object {
        const val SEGMENTS = 12
        const val CURVE_FACTOR = 0.15
    }
}
