package com.lloppy.audiolessons.story

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.StateFlow

sealed interface ExportState {
    data object Idle : ExportState
    data class Exporting(val progress: Float) : ExportState
    data class Done(val mp4Path: String) : ExportState
    data class Error(val message: String) : ExportState
}

interface StoryExporter {
    val state: StateFlow<ExportState>

    suspend fun export(attachment: ImageBitmap?, title: String, audioPath: String, durationMs: Long): String

    fun share(mp4Path: String)
}
