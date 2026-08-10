package com.blindspot.app.data.repository

import com.blindspot.app.data.remote.FavoriteRequest
import com.blindspot.app.data.remote.FavoritesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

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
            throw e
        } catch (e: Exception) {
            _favoritePlaceIds.update { current ->
                if (wasFavorite) current + placeId else current - placeId
            }
            throw e
        }
    }

    /** Clears local state on sign-out. Wrapped in [NonCancellable] so it always runs to
     * completion even if the caller's coroutine (e.g. a LaunchedEffect cancelled by a fast
     * sign-in immediately after sign-out) is being torn down — and because it's invoked inline,
     * before any subsequent refresh() call even exists, it reaches [mutex] first, so the fair
     * FIFO queue guarantees it completes before that refresh() can run. */
    suspend fun clear() = withContext(NonCancellable) {
        mutex.withLock {
            _favoritePlaceIds.value = emptySet()
        }
    }
}