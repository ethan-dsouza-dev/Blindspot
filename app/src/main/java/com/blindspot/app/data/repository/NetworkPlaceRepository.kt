package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Place
import com.blindspot.app.data.remote.NearestPlacesService

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

    override suspend fun getTrendingPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
    ): Result<List<Place>> = runCatching {
        nearestPlacesService.trending(latitude, longitude, radiusMeters)
    }

    override suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>> = runCatching {
        nearestPlacesService.byIds(ids)
    }
}