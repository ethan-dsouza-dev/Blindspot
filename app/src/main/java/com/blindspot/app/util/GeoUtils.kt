package com.blindspot.app.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure geo helpers used by the compass. Kept framework-free so they are trivially unit-testable.
 */
object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Initial great-circle bearing from one coordinate to another, in degrees clockwise from
     * true north, normalized to [0, 360).
     */
    fun bearingBetween(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
    ): Float {
        val fromLatRad = Math.toRadians(fromLat)
        val toLatRad = Math.toRadians(toLat)
        val deltaLngRad = Math.toRadians(toLng - fromLng)

        val y = sin(deltaLngRad) * cos(toLatRad)
        val x = cos(fromLatRad) * sin(toLatRad) -
            sin(fromLatRad) * cos(toLatRad) * cos(deltaLngRad)

        val bearingDeg = Math.toDegrees(atan2(y, x))
        return ((bearingDeg + 360.0) % 360.0).toFloat()
    }

    /**
     * Haversine distance between two coordinates in meters.
     */
    fun distanceMeters(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
    ): Double {
        val dLat = Math.toRadians(toLat - fromLat)
        val dLng = Math.toRadians(toLng - fromLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(fromLat)) * cos(Math.toRadians(toLat)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Formats a distance in meters into a short, human-friendly label.
     */
    fun formatDistance(meters: Double): String = when {
        meters < 1_000 -> "${meters.toInt()} m"
        else -> String.format("%.1f km", meters / 1_000)
    }

    /**
     * Normalizes any angle (degrees) to the range [0, 360).
     */
    fun normalizeDegrees(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
}
