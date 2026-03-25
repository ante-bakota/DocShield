package com.digitaldude.docshield

import android.app.Application
import com.digitaldude.docshield.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DocShieldApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DocShieldApp)
            modules(appModule)
        }
    }
}