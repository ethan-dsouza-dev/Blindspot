package com.blindspot.app.data.repository

import android.content.Context
import android.util.Log
import com.blindspot.app.data.remote.FcmTokenRequest
import com.blindspot.app.data.remote.NotificationsApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Persists the user's notification opt-in and syncs their FCM token with the backend. */
class NotificationsRepository(
    context: Context,
    private val notificationsApi: NotificationsApi,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()

        scope.launch {
            if (enabled) {
                fetchTokenAndRegister()
            } else {
                unregisterToken()
            }
        }
    }

    /** Called from [com.blindspot.app.notifications.BlindspotMessagingService] when FCM
     * rotates the token; only pushes it to the backend if the user is currently opted in. */
    fun registerTokenAsync(token: String) {
        if (!_notificationsEnabled.value) return
        scope.launch { registerToken(token) }
    }

    private suspend fun fetchTokenAndRegister() {
        runCatching { FirebaseMessaging.getInstance().token.await() }
            .onSuccess { token -> registerToken(token) }
            .onFailure { e -> Log.e(TAG, "Failed to fetch FCM token", e) }
    }

    private suspend fun registerToken(token: String) {
        runCatching { notificationsApi.registerFcmToken(FcmTokenRequest(token)) }
            .onFailure { e -> Log.e(TAG, "Failed to register FCM token with backend", e) }
    }

    private suspend fun unregisterToken() {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            notificationsApi.unregisterFcmToken(FcmTokenRequest(token))
        }.onFailure { e -> Log.e(TAG, "Failed to unregister FCM token with backend", e) }
    }

    private companion object {
        const val TAG = "NotificationsRepository"
        const val PREFS_NAME = "notifications_prefs"
        const val KEY_ENABLED = "notifications_enabled"
    }
}