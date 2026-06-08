package com.lloppy.audiolessons.audio.di

import com.lloppy.audiolessons.audio.AudioPlayer
import com.lloppy.audiolessons.audio.AvAudioPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    single<AudioPlayer> { AvAudioPlayer() }
}
