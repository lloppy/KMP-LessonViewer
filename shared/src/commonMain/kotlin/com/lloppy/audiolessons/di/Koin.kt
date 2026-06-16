package com.lloppy.audiolessons.di

import com.lloppy.audiolessons.AppViewModel
import com.lloppy.audiolessons.audio.di.audioModule
import com.lloppy.audiolessons.audio.di.platformAudioModule
import com.lloppy.audiolessons.story.di.platformStoryModule
import com.lloppy.audiolessons.library.LibraryRepository
import com.lloppy.audiolessons.library.scan.LibraryScanner
import com.lloppy.audiolessons.library.scan.RecursiveLibraryScanner
import com.lloppy.audiolessons.screens.courses.CoursesViewModel
import com.lloppy.audiolessons.screens.lessons.LessonsViewModel
import com.lloppy.audiolessons.screens.player.PlayerViewModel
import com.lloppy.audiolessons.screens.settings.SettingsViewModel
import com.lloppy.audiolessons.settings.SettingsRepository
import com.russhwolf.settings.Settings
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    single<Settings> { Settings() }
    single { SettingsRepository(get()) }
}

val libraryModule = module {
    single<LibraryScanner> { RecursiveLibraryScanner() }
    single { LibraryRepository(settings = get(), scanner = get()) }
}

val viewModelModule = module {
    factoryOf(::AppViewModel)
    factoryOf(::CoursesViewModel)
    factory { params ->
        LessonsViewModel(
            courseId = params.get(),
            repository = get(),
            progressRepository = get(),
        )
    }
    factory { params ->
        PlayerViewModel(
            lessonId = params.get(),
            library = get(),
            player = get(),
            progress = get(),
        )
    }
    factoryOf(::SettingsViewModel)
}

fun initKoin(config: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            libraryModule,
            audioModule,
            platformAudioModule,
            platformStoryModule,
            viewModelModule,
        )
    }
}
