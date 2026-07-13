package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Route

/**
 * Source of navigation [Route]s between two coordinates.
 *
 * Swap the bound implementation in the Koin module to move from [MockRouteRepository]
 * to a real network-backed one without touching the UI.
 */
interface RouteRepository {

    /**
     * Returns a route from the origin to the destination for the given travel [mode], wrapped in
     * a [Result] so callers can surface success/failure without handling exceptions directly.
     */
    suspend fun getRoute(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
        mode: String = DEFAULT_MODE,
    ): Result<Route>

    companion object {
        const val DEFAULT_MODE = "walk"
    }
}
