package com.blindspot.app.data.repository

import android.content.Context
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
                val token = FirebaseMessaging.getInstance().token.await()
                registerToken(token)
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

    private suspend fun registerToken(token: String) {
        runCatching { notificationsApi.registerFcmToken(FcmTokenRequest(token)) }
    }

    private suspend fun unregisterToken() {
        runCatching { notificationsApi.unregisterFcmToken() }
    }

    private companion object {
        const val PREFS_NAME = "notifications_prefs"
        const val KEY_ENABLED = "notifications_enabled"
    }
}