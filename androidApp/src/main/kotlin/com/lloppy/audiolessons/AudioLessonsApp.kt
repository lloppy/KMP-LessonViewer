package com.lloppy.audiolessons

import android.app.Application
import com.lloppy.audiolessons.di.initKoin
import org.koin.android.ext.koin.androidContext

class AudioLessonsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@AudioLessonsApp)
        }
    }
}
