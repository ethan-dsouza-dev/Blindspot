package com.blindspot.app.data.repository

import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that [MockPlaceRepository] honours the search radius and price point passed by the
 * discovery flow: only places within the radius are returned, a wider radius never returns fewer
 * places, and results match the requested price level.
 */
class MockPlaceRepositoryTest {

    private val repository = MockPlaceRepository()

    /** Fetches places across every price level so radius assertions are independent of price. */
    private suspend fun allPricesWithinRadius(radiusMeters: Int) =
        (PlaceRepository.MIN_PRICE_LEVEL..PlaceRepository.MAX_PRICE_LEVEL).flatMap { level ->
            repository.getNearbyPlaces(LAT, LNG, radiusMeters = radiusMeters, priceLevel = level)
                .getOrThrow()
        }

    @Test
    fun getNearbyPlaces_onlyReturnsPlacesWithinRadius() = runTest {
        val radius = 5_000

        val places = allPricesWithinRadius(radius)

        assertTrue(places.isNotEmpty())
        places.forEach { place ->
            val distance = GeoUtils.distanceMeters(LAT, LNG, place.latitude, place.longitude)
            assertTrue("expected ${place.name} within $radius m but was $distance", distance <= radius)
        }
    }

    @Test
    fun getNearbyPlaces_widerRadiusReturnsAtLeastAsMany() = runTest {
        val near = allPricesWithinRadius(300)
        val far = allPricesWithinRadius(5_000)

        assertTrue(far.size >= near.size)
    }

    @Test
    fun getNearbyPlaces_maxRadiusReturnsAllSamples() = runTest {
        val places = allPricesWithinRadius(5_000)

        assertEquals(5, places.size)
    }

    @Test
    fun getNearbyPlaces_onlyReturnsPlacesMatchingPriceLevel() = runTest {
        val priceLevel = 2

        val places = repository.getNearbyPlaces(
            LAT,
            LNG,
            radiusMeters = 5_000,
            priceLevel = priceLevel,
        ).getOrThrow()

        assertTrue(places.isNotEmpty())
        places.forEach { place ->
            assertEquals(priceLevel, place.priceLevel)
        }
    }

    private companion object {
        const val LAT = 40.0
        const val LNG = -73.0
    }
}
