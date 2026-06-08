package com.lloppy.audiolessons.audio

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.StateFlow

data class PlaybackState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val mediaId: String? = null,
)

interface AudioPlayer {
    val state: StateFlow<PlaybackState>

    fun load(mediaId: String, file: PlatformFile, startPositionMs: Long = 0L)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
}
