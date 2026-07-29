package com.blindspot.app.data.model

/**
 * A single coordinate along a route. Kept framework-free (plain lat/lng) so the routing layer
 * and its decoder don't depend on the map SDK; the UI maps these to map positions when drawing.
 */
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
)

/**
 * A navigable route between two points, as returned by the routing backend.
 *
 * [points] is the decoded geometry (ordered origin → destination) used to draw the route line.
 * [distanceMeters] and [durationSeconds] describe the trip when the backend provides them.
 */
data class Route(
    val points: List<RoutePoint>,
    val distanceMeters: Double? = null,
    val durationSeconds: Double? = null,
)
