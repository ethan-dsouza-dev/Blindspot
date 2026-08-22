package com.blindspot.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Body

data class FcmTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String,
)

interface NotificationsApi {

    @POST("api/notifications/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest)

    @DELETE("api/notifications/fcm-token")
    suspend fun unregisterFcmToken()
}