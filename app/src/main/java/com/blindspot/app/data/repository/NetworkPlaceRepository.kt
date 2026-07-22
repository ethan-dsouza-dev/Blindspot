package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Place
import com.blindspot.app.data.remote.NearestPlacesService

/**
 * Live [PlaceRepository] backed by the Blindspot backend via [NearestPlacesService].
 *
 * Delegates the network call to the service and wraps the result in [Result] so the
 * ViewModel can surface success/failure without handling exceptions directly.
 */
class NetworkPlaceRepository(
    private val nearestPlacesService: NearestPlacesService,
) : PlaceRepository {

    override suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        priceLevel: Int?,
        category: String,
    ): Result<List<Place>> = runCatching {
        nearestPlacesService.nearby(latitude, longitude, radiusMeters, priceLevel)
    }
}
