package com.blindspot.app

import android.app.Application
import com.blindspot.app.di.appModule
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
    }
}
