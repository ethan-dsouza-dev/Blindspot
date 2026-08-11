package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Place

interface PlaceRepository {

    suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_RADIUS_METERS,
        priceLevel: Int? = null,
        category: String = "bar",
    ): Result<List<Place>>

    suspend fun getTrendingPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_TRENDING_RADIUS_METERS,
    ): Result<List<Place>>

    /** Resolves a set of place IDs to full [Place] objects, e.g. for rendering favorites. */
    suspend fun getPlacesByIds(ids: List<String>): Result<List<Place>>

    companion object {
        const val MIN_RADIUS_METERS = 150
        const val MAX_RADIUS_METERS = 5_000
        const val DEFAULT_RADIUS_METERS = 1_000
        const val DEFAULT_TRENDING_RADIUS_METERS = 5_000
        const val MIN_PRICE_LEVEL = 1
        const val MAX_PRICE_LEVEL = 4
    }
}