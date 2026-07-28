package com.blindspot.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the routing endpoint.
 *
 * Maps to `GET /routing/route?originLat=&originLng=&destLat=&destLng=&mode=` on the Blindspot
 * backend, which proxies the Geoapify Routing API and returns the geometry as an encoded polyline.
 * Returns 400 for out-of-range lat/lng and 404 when Geoapify finds no route.
 */
interface RouteApi {

    @GET("routing/route")
    suspend fun getRoute(
        @Query("originLat") originLatitude: Double,
        @Query("originLng") originLongitude: Double,
        @Query("destLat") destLatitude: Double,
        @Query("destLng") destLongitude: Double,
        // TODO: expose mode selection once the UI supports switching travel modes; the backend
        // already accepts this optional param (defaults to "walk").
        @Query("mode") mode: String,
    ): RouteDto
}
