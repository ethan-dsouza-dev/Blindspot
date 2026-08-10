package com.blindspot.app.data.repository

import com.blindspot.app.data.remote.FavoriteRequest
import com.blindspot.app.data.remote.FavoritesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
) {
    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    private val mutex = Mutex()

    // Repository-owned, not tied to any composable's lifecycle — a caller invoking clearAsync()
    // as fire-and-forget from a LaunchedEffect that then gets cancelled (e.g. rapid sign-out /
    // re-auth) can't take this scope down with it.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    suspend fun clear() = mutex.withLock {
        _favoritePlaceIds.value = emptySet()
    }

    /** Fire-and-forget clear for callers (e.g. sign-out) that must not block on it. Runs on the
     * repository's own scope so it survives cancellation of whatever caller triggered it. */
    fun clearAsync() {
        scope.launch { clear() }
    }
}