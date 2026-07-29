package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Route
import com.blindspot.app.data.remote.RoutingService
import retrofit2.HttpException

/**
 * Live [RouteRepository] backed by the Blindspot backend via [RoutingService].
 *
 * Delegates the network call to the service and wraps the result in [Result] so callers can
 * surface success/failure without handling exceptions directly. HTTP 400/404 responses are
 * translated into [RouteError.InvalidLocation]/[RouteError.NoRouteFound] so callers can
 * distinguish them from other failures.
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
    ): Result<Route> = try {
        Result.success(
            routingService.route(fromLatitude, fromLongitude, toLatitude, toLongitude, mode),
        )
    } catch (e: HttpException) {
        Result.failure(
            when (e.code()) {
                400 -> RouteError.InvalidLocation(e)
                404 -> RouteError.NoRouteFound(e)
                else -> e
            },
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}
