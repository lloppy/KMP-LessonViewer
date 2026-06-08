package com.lloppy.audiolessons.audio.di

import com.lloppy.audiolessons.audio.AudioPlayer
import com.lloppy.audiolessons.audio.Media3AudioPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    single<AudioPlayer> { Media3AudioPlayer(androidContext()) }
}
