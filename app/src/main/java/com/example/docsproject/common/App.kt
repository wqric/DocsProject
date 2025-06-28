package com.example.docsproject.common

import android.app.Application
import android.graphics.Bitmap
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App(): Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(mainModule)
        }
    }
}