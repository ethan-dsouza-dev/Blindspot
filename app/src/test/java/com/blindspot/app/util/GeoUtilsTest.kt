package com.blindspot.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [GeoUtils]. These verify the compass math (bearing, distance) and the
 * formatting/normalization helpers used by the discovery feature.
 */
class GeoUtilsTest {

    // region bearingBetween

    @Test
    fun bearingBetween_pointsNorth_returns0() {
        val bearing = GeoUtils.bearingBetween(0.0, 0.0, 1.0, 0.0)
        assertEquals(0f, bearing, BEARING_TOLERANCE)
    }

    @Test
    fun bearingBetween_pointsEast_returns90() {
        val bearing = GeoUtils.bearingBetween(0.0, 0.0, 0.0, 1.0)
        assertEquals(90f, bearing, BEARING_TOLERANCE)
    }

    @Test
    fun bearingBetween_pointsSouth_returns180() {
        val bearing = GeoUtils.bearingBetween(0.0, 0.0, -1.0, 0.0)
        assertEquals(180f, bearing, BEARING_TOLERANCE)
    }

    @Test
    fun bearingBetween_pointsWest_returns270() {
        val bearing = GeoUtils.bearingBetween(0.0, 0.0, 0.0, -1.0)
        assertEquals(270f, bearing, BEARING_TOLERANCE)
    }

    @Test
    fun bearingBetween_isAlwaysNormalized() {
        val bearing = GeoUtils.bearingBetween(40.0, -74.0, 41.0, -73.0)
        assert(bearing in 0f..360f)
    }

    // endregion

    // region distanceMeters

    @Test
    fun distanceMeters_samepoint_isZero() {
        val distance = GeoUtils.distanceMeters(51.5, -0.12, 51.5, -0.12)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun distanceMeters_oneDegreeOfLongitudeAtEquator_isAbout111km() {
        val distance = GeoUtils.distanceMeters(0.0, 0.0, 0.0, 1.0)
        // One degree of longitude at the equator is ~111.19 km.
        assertEquals(111_195.0, distance, 500.0)
    }

    @Test
    fun distanceMeters_isSymmetric() {
        val a = GeoUtils.distanceMeters(48.85, 2.35, 40.71, -74.0)
        val b = GeoUtils.distanceMeters(40.71, -74.0, 48.85, 2.35)
        assertEquals(a, b, 0.001)
    }

    // endregion

    // region normalizeDegrees

    @Test
    fun normalizeDegrees_negativeWrapsToPositive() {
        assertEquals(270f, GeoUtils.normalizeDegrees(-90f), 0.0001f)
    }

    @Test
    fun normalizeDegrees_overflowWraps() {
        assertEquals(90f, GeoUtils.normalizeDegrees(450f), 0.0001f)
    }

    @Test
    fun normalizeDegrees_fullCircleIsZero() {
        assertEquals(0f, GeoUtils.normalizeDegrees(360f), 0.0001f)
    }

    @Test
    fun normalizeDegrees_inRangeIsUnchanged() {
        assertEquals(123.4f, GeoUtils.normalizeDegrees(123.4f), 0.0001f)
    }

    // endregion

    // region formatDistance

    @Test
    fun formatDistance_belowOneKm_usesMeters() {
        assertEquals("500 m", GeoUtils.formatDistance(500.0))
        assertEquals("999 m", GeoUtils.formatDistance(999.0))
    }

    @Test
    fun formatDistance_atOrAboveOneKm_usesKilometers() {
        assertEquals("1.0 km", GeoUtils.formatDistance(1_000.0))
        assertEquals("2.5 km", GeoUtils.formatDistance(2_500.0))
    }

    // endregion

    private companion object {
        const val BEARING_TOLERANCE = 0.01f
    }
}
