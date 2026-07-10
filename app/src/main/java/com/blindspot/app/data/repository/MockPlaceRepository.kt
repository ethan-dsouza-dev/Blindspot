package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Place
import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.delay

/**
 * Temporary in-memory [PlaceRepository] used until the backend endpoint exists.
 *
 * Generates a handful of places scattered around the user's current location so the compass
 * has realistic, nearby targets. Returns them sorted nearest-first.
 */
class MockPlaceRepository : PlaceRepository {

    override suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        category: String,
    ): Result<List<Place>> = runCatching {
        // Simulate network latency.
        delay(600)

        SAMPLE_OFFSETS.mapIndexed { index, offset ->
            val placeLat = latitude + offset.latOffset
            val placeLng = longitude + offset.lngOffset
            Place(
                id = "place_$index",
                name = offset.name,
                description = offset.description,
                category = category,
                latitude = placeLat,
                longitude = placeLng,
                imageUrl = offset.imageUrl,
                rating = offset.rating,
                priceLevel = offset.priceLevel,
            )
        }.filter { place ->
            GeoUtils.distanceMeters(latitude, longitude, place.latitude, place.longitude) <= radiusMeters
        }.sortedBy { place ->
            GeoUtils.distanceMeters(latitude, longitude, place.latitude, place.longitude)
        }
    }

    private data class SampleOffset(
        val name: String,
        val description: String,
        val latOffset: Double,
        val lngOffset: Double,
        val rating: Double,
        val priceLevel: Int,
        val imageUrl: List<String>,
    )

    private companion object {
        // ~0.001 degrees latitude is roughly 111 m.
        val SAMPLE_OFFSETS = listOf(
            SampleOffset(
                name = "The Hidden Flask",
                description = "Cozy speakeasy with craft cocktails and dim, moody lighting.",
                latOffset = 0.0018, lngOffset = 0.0009, rating = 4.6, priceLevel = 3,
                imageUrl = listOf(
                    "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800",
                    "https://images.unsplash.com/photo-1470337458703-46ad1756a187?w=800",
                    "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800",
                ),
            ),
            SampleOffset(
                name = "Neon Alley",
                description = "Buzzing rooftop bar with skyline views and house DJs on weekends.",
                latOffset = -0.0022, lngOffset = 0.0015, rating = 4.3, priceLevel = 2,
                imageUrl = listOf(
                    "https://images.unsplash.com/photo-1566417713940-fe7c737a9ef2?w=800",
                    "https://images.unsplash.com/photo-1485872299829-c673f5194813?w=800",
                ),
            ),
            SampleOffset(
                name = "Cellar 9",
                description = "Underground wine bar pouring natural wines and small plates.",
                latOffset = 0.0009, lngOffset = -0.0026, rating = 4.8, priceLevel = 3,
                imageUrl = listOf(
                    "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=800",
                ),
            ),
            SampleOffset(
                name = "The Tap Room",
                description = "Laid-back pub with 24 rotating local craft beers on tap.",
                latOffset = -0.0014, lngOffset = -0.0011, rating = 4.1, priceLevel = 1,
                imageUrl = listOf(
                    "https://images.unsplash.com/photo-1436076863939-06870fe779c2?w=800",
                    "https://images.unsplash.com/photo-1518176258769-f227c798150e?w=800",
                ),
            ),
            SampleOffset(
                name = "Midnight Lounge",
                description = "Late-night jazz lounge serving classic cocktails until 3am.",
                latOffset = 0.0031, lngOffset = 0.0024, rating = 4.5, priceLevel = 2,
                imageUrl = listOf(
                    "https://images.unsplash.com/photo-1572116469696-31de0f17cc34?w=800",
                ),
            ),
        )
    }
}
