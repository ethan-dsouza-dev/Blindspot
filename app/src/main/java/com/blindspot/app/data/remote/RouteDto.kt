package com.blindspot.app.data.remote

import com.blindspot.app.data.model.Route
import com.blindspot.app.util.PolylineCodec
import com.google.gson.annotations.SerializedName

/**
 * Network representation of a route returned by the Blindspot backend, which proxies the
 * Geoapify Routing API. The geometry ships as an encoded polyline string (Google Encoded
 * Polyline Algorithm Format) that the client decodes for rendering.
 *
 * Kept separate from the domain [Route] so the API contract can evolve independently.
 */
data class RouteDto(
    @SerializedName("polyline") val polyline: String,
    @SerializedName("precision") val precision: Int? = null,
    @SerializedName("distance_meters") val distanceMeters: Double? = null,
    @SerializedName("duration_seconds") val durationSeconds: Double? = null,
)

fun RouteDto.toDomain(): Route = Route(
    points = PolylineCodec.decode(polyline, precision ?: PolylineCodec.DEFAULT_PRECISION),
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
)
