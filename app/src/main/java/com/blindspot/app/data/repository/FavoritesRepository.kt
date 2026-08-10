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

class FavoritesRepository(
    private val favoritesApi: FavoritesApi,
) {
    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    private val mutex = Mutex()

    // Completed by the first successful refresh() of a session. toggleFavorite() awaits this
    // before deriving wasFavorite from local state, so a toggle firing before the initial load
    // finishes can never race it — regardless of which coroutine's mutex.withLock actually runs
    // first. Reset by clear() so the next account's toggles wait for its own fresh load.
    @Volatile
    private var initialized = CompletableDeferred<Unit>()

    suspend fun refresh() = mutex.withLock {
        val response = favoritesApi.getFavorites()
        _favoritePlaceIds.value = response.placeIds.toSet()
        if (!initialized.isCompleted) initialized.complete(Unit)
    }

    suspend fun toggleFavorite(placeId: String) {
        initialized.await()
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
}