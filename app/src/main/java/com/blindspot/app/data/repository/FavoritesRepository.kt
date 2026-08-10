package com.blindspot.app.data.repository

import com.blindspot.app.data.remote.FavoriteRequest
import com.blindspot.app.data.remote.FavoritesApi
import kotlinx.coroutines.CancellationException
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
 * [mutex] serializes refresh(), toggleFavorite(), and clear() against each other — every
 * mutation to [_favoritePlaceIds] goes through it, so sign-out can never interleave with an
 * in-flight toggle's rollback, and a refresh can never land mid-toggle.
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
        } catch (e: CancellationException) {
            // The screen/session is going away (e.g. sign-out). Don't roll back: a rollback
            // here would write this account's data back into the singleton after clear() may
            // have already reset it for the next account, leaking stale favorites.
            throw e
        } catch (e: Exception) {
            _favoritePlaceIds.update { current ->
                if (wasFavorite) current + placeId else current - placeId
            }
            throw e
        }
    }

    /** Clears local state on sign-out. Mutex-protected so it can never interleave with an
     * in-flight refresh() or toggleFavorite() — it either runs fully before or fully after
     * any in-progress mutation, never in the middle of one. */
    suspend fun clear() = mutex.withLock {
        _favoritePlaceIds.value = emptySet()
    }
}