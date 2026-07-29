package com.blindspot.app.di

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
import com.blindspot.app.ui.discovery.DiscoveryViewModel
import com.blindspot.app.ui.profile.ProfileViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://blindspot-api/"

val appModule = module {

    single {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(PlaceApi::class.java) }
    single { NearestPlacesService(get()) }

    single { get<Retrofit>().create(RouteApi::class.java) }
    single { RoutingService(get()) }

    // Live, network-backed implementation. MockPlaceRepository remains available for testing.
    single<PlaceRepository> { NetworkPlaceRepository(get()) }

    single<RouteRepository> { NetworkRouteRepository(get()) }

    single { LocationProvider(androidContext()) }
    single { CompassSensorManager(androidContext()) }

    viewModel { DiscoveryViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel() }
}
