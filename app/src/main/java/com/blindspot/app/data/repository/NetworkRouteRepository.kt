package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Route
import com.blindspot.app.data.remote.RoutingService

/**
 * Live [RouteRepository] backed by the Blindspot backend via [RoutingService].
 *
 * Delegates the network call to the service and wraps the result in [Result] so callers can
 * surface success/failure without handling exceptions directly.
 */
class NetworkRouteRepository(
    private val routingService: RoutingService,
) : RouteRepository {

    override suspend fun getRoute(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
        mode: String,
    ): Result<Route> = runCatching {
        routingService.route(fromLatitude, fromLongitude, toLatitude, toLongitude, mode)
    }
}
