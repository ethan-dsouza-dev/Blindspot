package com.blindspot.app.data.repository

/**
 * Typed failures for [RouteRepository.getRoute], distinguishing the backend's documented error
 * responses so callers can surface a specific message instead of a generic failure.
 */
sealed class RouteError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The backend returned 400: origin/destination latitude or longitude was out of range. */
    class InvalidLocation(cause: Throwable? = null) :
        RouteError("Invalid latitude/longitude for route request", cause)

    /** The backend returned 404: Geoapify could not find a route between the two points. */
    class NoRouteFound(cause: Throwable? = null) :
        RouteError("No route found between the given locations", cause)
}
