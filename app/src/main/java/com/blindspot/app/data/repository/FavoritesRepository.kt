package com.blindspot.app.data.repository

import com.blindspot.app.data.remote.FavoriteRequest
import com.blindspot.app.data.remote.FavoritesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the current user's favorited place IDs as shared state so every screen showing
 * [com.blindspot.app.ui.components.PlaceInfoSheet] reflects the same favorite status.
 * Applies an optimistic update on toggle and rolls back if the network call fails.
 */
class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
) {
    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    suspend fun refresh() {
        val response = favoritesApi.getFavorites()
        _favoritePlaceIds.value = response.placeIds.toSet()
    }

    suspend fun toggleFavorite(placeId: String) {
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
            // Roll back the optimistic update on failure.
            _favoritePlaceIds.update { current ->
                if (wasFavorite) current + placeId else current - placeId
            }
            throw e
        }
    }
}