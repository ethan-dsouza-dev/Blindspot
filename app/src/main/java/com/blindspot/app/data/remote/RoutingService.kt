package com.blindspot.app.data.remote

import com.blindspot.app.data.model.Route

/**
 * Thin service over [RouteApi] that fetches a route between two coordinates and maps the network
 * [RouteDto] (encoded polyline) to a domain [Route] with decoded geometry. Keeps the Retrofit
 * contract isolated from callers.
 */
class RoutingService(
    private val api: RouteApi,
) {

    /**
     * Returns the route from ([fromLatitude], [fromLongitude]) to ([toLatitude], [toLongitude])
     * for the given travel [mode]. Network/parsing failures propagate to the caller.
     */
    suspend fun route(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
        mode: String = DEFAULT_MODE,
    ): Route =
        api.getRoute(fromLatitude, fromLongitude, toLatitude, toLongitude, mode).toDomain()

    companion object {
        const val DEFAULT_MODE = "walk"
    }
}
