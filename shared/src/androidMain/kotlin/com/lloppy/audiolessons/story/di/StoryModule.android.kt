package com.lloppy.audiolessons.story.di

import androidx.media3.common.util.UnstableApi
import com.lloppy.audiolessons.story.AndroidVoiceRecorder
import com.lloppy.audiolessons.story.Media3StoryExporter
import com.lloppy.audiolessons.story.StoryExporter
import com.lloppy.audiolessons.story.StoryViewModel
import com.lloppy.audiolessons.story.VoiceRecorder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(UnstableApi::class)
actual val platformStoryModule: Module = module {
    single<VoiceRecorder> { AndroidVoiceRecorder(androidContext()) }
    single<StoryExporter> { Media3StoryExporter(androidContext()) }
    factory { params ->
        StoryViewModel(
            lessonId = params.get(),
            library = get(),
            recorder = get(),
            exporter = get(),
        )
    }
}

actual val isStoryAvailable: Boolean = true
