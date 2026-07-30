package com.lloppy.moscow_nfc

import android.app.Application
import com.lloppy.moscow_nfc.di.initKoin
import org.koin.android.ext.koin.androidContext

class MoscowNfcApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MoscowNfcApp)
        }
    }
}
