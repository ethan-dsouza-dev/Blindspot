package com.blindspot.app.data.repository

import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies [MockRouteRepository] returns a decoded, ordered route whose endpoints match the
 * requested origin/destination and whose reported distance is positive.
 */
class MockRouteRepositoryTest {

    private val repository = MockRouteRepository()

    @Test
    fun getRoute_startsAtOriginAndEndsAtDestination() = runTest {
        val route = repository.getRoute(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG).getOrThrow()

        assertTrue("expected multiple points", route.points.size > 2)

        val first = route.points.first()
        assertEquals(FROM_LAT, first.latitude, 1e-4)
        assertEquals(FROM_LNG, first.longitude, 1e-4)

        val last = route.points.last()
        assertEquals(TO_LAT, last.latitude, 1e-4)
        assertEquals(TO_LNG, last.longitude, 1e-4)
    }

    @Test
    fun getRoute_reportsPositiveDistanceAndDuration() = runTest {
        val route = repository.getRoute(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG).getOrThrow()

        val straightLine = GeoUtils.distanceMeters(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG)
        val distance = route.distanceMeters
        val duration = route.durationSeconds
        assertTrue(distance != null && distance >= straightLine)
        assertTrue(duration != null && duration > 0.0)
    }

    private companion object {
        const val FROM_LAT = 40.7128
        const val FROM_LNG = -74.0060
        const val TO_LAT = 40.7306
        const val TO_LNG = -73.9866
    }
}
