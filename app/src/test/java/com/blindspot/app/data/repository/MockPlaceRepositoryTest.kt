package com.blindspot.app.data.repository

import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that [MockPlaceRepository] honours the search radius passed by the discovery flow:
 * only places within the radius are returned, and a wider radius never returns fewer places.
 */
class MockPlaceRepositoryTest {

    private val repository = MockPlaceRepository()

    @Test
    fun getNearbyPlaces_onlyReturnsPlacesWithinRadius() = runTest {
        val radius = 300

        val places = repository.getNearbyPlaces(LAT, LNG, radiusMeters = radius).getOrThrow()

        assertTrue(places.isNotEmpty())
        places.forEach { place ->
            val distance = GeoUtils.distanceMeters(LAT, LNG, place.latitude, place.longitude)
            assertTrue("expected ${place.name} within $radius m but was $distance", distance <= radius)
        }
    }

    @Test
    fun getNearbyPlaces_widerRadiusReturnsAtLeastAsMany() = runTest {
        val near = repository.getNearbyPlaces(LAT, LNG, radiusMeters = 300).getOrThrow()
        val far = repository.getNearbyPlaces(LAT, LNG, radiusMeters = 5_000).getOrThrow()

        assertTrue(far.size >= near.size)
    }

    @Test
    fun getNearbyPlaces_maxRadiusReturnsAllSamples() = runTest {
        val places = repository.getNearbyPlaces(LAT, LNG, radiusMeters = 5_000).getOrThrow()

        assertEquals(5, places.size)
    }

    private companion object {
        const val LAT = 40.0
        const val LNG = -73.0
    }
}
