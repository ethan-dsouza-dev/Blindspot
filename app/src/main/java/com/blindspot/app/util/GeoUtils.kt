package com.blindspot.app.util

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure geo helpers used by the compass. Kept framework-free so they are trivially unit-testable.
 */
object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val METERS_TO_FEET = 3.28084
    private const val FEET_PER_MILE = 5_280.0
    private const val FEET_DISPLAY_THRESHOLD = 1_000.0

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
     * Formats a distance in meters into a short, human-friendly label, in either metric
     * (meters/kilometers) or imperial (feet/miles) depending on [useKilometers].
     */
    fun formatDistance(meters: Double, useKilometers: Boolean = true): String {
        return if (useKilometers) {
            when {
                meters < 1_000 -> "${meters.toInt()} m"
                else -> String.format(Locale.US, "%.1f km", meters / 1_000)
            }
        } else {
            val feet = meters * METERS_TO_FEET
            when {
                feet < FEET_DISPLAY_THRESHOLD -> "${feet.toInt()} ft"
                else -> String.format(Locale.US, "%.1f mi", feet / FEET_PER_MILE)
            }
        }
    }

    fun normalizeDegrees(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
}