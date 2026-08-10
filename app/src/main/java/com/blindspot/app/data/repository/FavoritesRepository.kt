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
 *
 * [mutex] serializes refresh() and toggleFavorite() against each other — without this, a
 * refresh landing mid-toggle could either erase a successful optimistic update or have a
 * toggle roll back against data the refresh just replaced.
 */
class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
) {
    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    private val mutex = Mutex()

    suspend fun refresh() = mutex.withLock {
        val response = favoritesApi.getFavorites()
        _favoritePlaceIds.value = response.placeIds.toSet()
    }

    suspend fun toggleFavorite(placeId: String) = mutex.withLock {
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

    /** Clears local state on sign-out, so a different account signing in on the same session
     * never briefly sees the previous user's favorites before its own refresh completes. */
    fun clear() {
        _favoritePlaceIds.value = emptySet()
    }
}