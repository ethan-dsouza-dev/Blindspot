package com.blindspot.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the bars endpoints.
 *
 * Maps to `GET /bars/nearby?lat=&lng=&radius=&priceLevel=` and
 * `GET /bars/trending?lat=&lng=&radius=` on the Blindspot backend.
 */
interface PlaceApi {

    @GET("bars/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusMeters: Int,
        @Query("priceLevel") priceLevel: Int?,
    ): List<PlaceDto>

    @GET("bars/trending")
    suspend fun getTrendingPlaces(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusMeters: Int,
    ): List<PlaceDto>

    @GET("bars/byIds")
    suspend fun getPlacesByIds(
        @Query("ids") ids: List<String>,
    ): List<PlaceDto>
}
