package com.blindspot.app.di

import com.blindspot.app.data.remote.PlaceApi
import com.blindspot.app.data.repository.MockPlaceRepository
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.location.LocationProvider
import com.blindspot.app.sensor.CompassSensorManager
import com.blindspot.app.ui.discovery.DiscoveryViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Base URL placeholder until the real backend is available. Swap this (and the repository
 * binding below) to move from the mock to live data.
 */
private const val BASE_URL = "https://api.blindspot.example/"

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

    // Mock for now; replace with a network-backed PlaceRepository when the endpoint is ready.
    single<PlaceRepository> { MockPlaceRepository() }

    single { LocationProvider(androidContext()) }
    single { CompassSensorManager(androidContext()) }

    viewModel { DiscoveryViewModel(get(), get(), get()) }
}
