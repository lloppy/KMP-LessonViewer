package com.lloppy.akta

import android.app.Application
import com.lloppy.akta.di.initKoin
import org.koin.android.ext.koin.androidContext

class AktaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@AktaApp)
        }
    }
}
