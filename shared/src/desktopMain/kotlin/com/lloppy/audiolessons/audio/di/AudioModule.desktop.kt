package com.lloppy.audiolessons.audio.di

import com.lloppy.audiolessons.audio.AudioPlayer
import com.lloppy.audiolessons.audio.JavaFxAudioPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    single<AudioPlayer> { JavaFxAudioPlayer() }
}
