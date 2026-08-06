package com.blindspot.app

import android.app.Application
import com.blindspot.app.di.appModule
import com.blindspot.app.ui.ads.AdsConfig
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point. Starts Koin so ViewModels and services can be injected app-wide.
 * Named `BlindspotApplication` to avoid confusion with the `BlindspotApp` Compose root.
 */
class BlindspotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BlindspotApplication)
            modules(appModule)
        }
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                this@BlindspotApplication,
                InitializationConfig.Builder(AdsConfig.APPLICATION_ID).build(),
            ) { }
        }
    }
}
