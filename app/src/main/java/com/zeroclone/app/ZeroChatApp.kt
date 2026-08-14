package com.zeroclone.app

import android.app.Application
import com.zeroclone.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZeroChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ZeroChatApp)
            modules(appModule)
        }
    }
}
