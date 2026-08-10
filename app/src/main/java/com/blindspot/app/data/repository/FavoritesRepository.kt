package com.blindspot.app.data.repository

import com.blindspot.app.data.remote.FavoriteRequest
import com.blindspot.app.data.remote.FavoritesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Holds the current user's favorited place IDs as shared state so every screen showing
 * [com.blindspot.app.ui.components.PlaceInfoSheet] reflects the same favorite status.
 * Applies an optimistic update on toggle and rolls back if the network call fails.
 *
 * [toggleMutex] serializes toggles so concurrent taps (e.g. rapid double-tap, or toggling from
 * two screens at once) can't interleave — each toggle's read-update-rollback runs atomically
 * with respect to the others, preventing a failed request from undoing a different successful one.
 */
class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
) {
    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    private val toggleMutex = Mutex()

    suspend fun refresh() {
        val response = favoritesApi.getFavorites()
        _favoritePlaceIds.value = response.placeIds.toSet()
    }

    suspend fun toggleFavorite(placeId: String) = toggleMutex.withLock {
        val wasFavorite = placeId in _favoritePlaceIds.value

        _favoritePlaceIds.update { current ->
            if (wasFavorite) current - placeId else current + placeId
        }

        try {
            if (wasFavorite) {
                favoritesApi.removeFavorite(placeId)
            } else {
                favoritesApi.addFavorite(FavoriteRequest(placeId))
            }
        } catch (e: Exception) {
            _favoritePlaceIds.update { current ->
                if (wasFavorite) current + placeId else current - placeId
            }
            throw e
        }
    }
}