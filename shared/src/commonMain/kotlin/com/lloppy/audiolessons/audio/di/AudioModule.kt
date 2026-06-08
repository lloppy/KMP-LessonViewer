package com.lloppy.audiolessons.audio.di

import com.lloppy.audiolessons.audio.ProgressRepository
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformAudioModule: Module

val audioModule = module {
    single { ProgressRepository(get()) }
}
