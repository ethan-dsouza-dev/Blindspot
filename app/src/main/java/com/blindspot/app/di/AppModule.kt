package com.blindspot.app.di

import com.blindspot.app.auth.AuthInterceptor
import com.blindspot.app.auth.AuthRepository
import com.blindspot.app.auth.AuthViewModel
import com.blindspot.app.auth.TokenRefreshAuthenticator
import com.blindspot.app.auth.TokenStore
import com.blindspot.app.data.remote.AuthApi
import com.blindspot.app.data.remote.NearestPlacesService
import com.blindspot.app.data.remote.PlaceApi
import com.blindspot.app.data.remote.RouteApi
import com.blindspot.app.data.remote.RoutingService
import com.blindspot.app.data.repository.NetworkPlaceRepository
import com.blindspot.app.data.repository.NetworkRouteRepository
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.data.repository.RouteRepository
import com.blindspot.app.location.LocationProvider
import com.blindspot.app.sensor.CompassSensorManager
import com.blindspot.app.ui.discovery.PlacesViewModel
import com.blindspot.app.ui.profile.ProfileViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.blindspot.example/"
private const val GOOGLE_SERVER_CLIENT_ID = "566311996955-g244lgku4hoarbvkd1874ocrfcd9nnvr.apps.googleusercontent.com"

val appModule = module {

    single { TokenStore(androidContext()) }

    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC } }

    // No-auth OkHttp/Retrofit for the auth endpoints themselves, so AuthApi does not
    // depend on the main OkHttpClient that uses TokenRefreshAuthenticator.
    single<OkHttpClient>(named("auth")) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single<Retrofit>(named("auth")) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>(named("auth")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AuthApi> { get<Retrofit>(named("auth")).create(AuthApi::class.java) }

    single { AuthRepository(androidContext(), get(), get()) }

    single { AuthInterceptor(get()) }
    single { TokenRefreshAuthenticator(get(), get()) }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenRefreshAuthenticator>())
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(PlaceApi::class.java) }
    single { get<Retrofit>().create(RouteApi::class.java) }

    single { NearestPlacesService(get()) }
    single { RoutingService(get()) }

    // Live, network-backed implementation. MockPlaceRepository remains available for testing.
    single<PlaceRepository> { NetworkPlaceRepository(get()) }
    single<RouteRepository> { NetworkRouteRepository(get()) }

    single { LocationProvider(androidContext()) }
    single { CompassSensorManager(androidContext()) }

    viewModel { AuthViewModel(get(), GOOGLE_SERVER_CLIENT_ID) }
    viewModel { PlacesViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get()) }
}
