package com.blindspot.app.data.repository

import com.blindspot.app.data.remote.FavoriteRequest
import com.blindspot.app.data.remote.FavoritesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
) {
    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    private val mutex = Mutex()

    @Volatile
    private var initialized = CompletableDeferred<Unit>()

    suspend fun refresh() = mutex.withLock {
        val response = favoritesApi.getFavorites()
        _favoritePlaceIds.value = response.placeIds.toSet()
        if (!initialized.isCompleted) initialized.complete(Unit)
    }

    suspend fun toggleFavorite(placeId: String) {
        // Wait for the initial load so a toggle right after sign-in doesn't act on a still-empty
        // set — but only up to INIT_WAIT_TIMEOUT_MS. If refresh() keeps failing (offline, outage),
        // give up waiting and proceed on whatever local state exists rather than hanging the UI
        // forever; the toggle still applies its own optimistic update + rollback below, and a
        // later successful refresh() reconciles anything this got wrong.
        withTimeoutOrNull(INIT_WAIT_TIMEOUT_MS) { initialized.await() }

        mutex.withLock {
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
    }

    suspend fun clear() = withContext(NonCancellable) {
        mutex.withLock {
            _favoritePlaceIds.value = emptySet()
            initialized = CompletableDeferred()
        }
    }

    private companion object {
        const val INIT_WAIT_TIMEOUT_MS = 5_000L
    }
}