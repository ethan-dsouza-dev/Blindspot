package com.blindspot.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

data class FcmTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String,
)

interface NotificationsApi {

    @POST("api/notifications/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest)

    // Plain @DELETE doesn't allow a @Body (Retrofit only sets hasBody for POST/PUT/PATCH),
    // so this needs @HTTP with hasBody explicitly set to send the token being unregistered.
    @HTTP(method = "DELETE", path = "api/notifications/fcm-token", hasBody = true)
    suspend fun unregisterFcmToken(@Body request: FcmTokenRequest)
}