package com.lloppy.audiolessons.story

import kotlinx.coroutines.flow.StateFlow

data class RecordingState(
    val isRecording: Boolean = false,
    val elapsedMs: Long = 0L,
    val amplitude: Float = 0f,
)

data class Recording(
    val path: String,
    val durationMs: Long,
)

interface VoiceRecorder {
    val state: StateFlow<RecordingState>

    fun start()
    fun stop(): Recording?
    fun cancel()
    fun release()
}
