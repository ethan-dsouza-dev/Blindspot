package com.blindspot.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.blindspot.app.R
import com.blindspot.app.data.repository.NotificationsRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

/** Receives FCM pushes and registers fresh tokens with the backend. Runs even when the app
 * is backgrounded or force-quit, since FCM wakes the process for delivery. */
class BlindspotMessagingService : FirebaseMessagingService() {

    private val notificationsRepository: NotificationsRepository by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        notificationsRepository.registerTokenAsync(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "Blindspot"
        val body = message.notification?.body ?: "Check out what's new nearby"
        showNotification(title, body)
    }

    // checkSelfPermission already returns PERMISSION_GRANTED automatically on API < 33 (the
    // permission isn't enforced there), so no separate SDK_INT gate is needed. Suppressed
    // since lint's static flow analysis doesn't always trace this runtime check reliably.
    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, body: String) {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val channelId = "discover_reminders"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Discover reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Occasional nudges to check out new places"
                },
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}