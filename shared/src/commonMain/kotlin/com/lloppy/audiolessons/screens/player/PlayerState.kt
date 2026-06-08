package com.lloppy.audiolessons.screens.player

import com.lloppy.audiolessons.audio.PlaybackState
import com.lloppy.audiolessons.library.model.Lesson

data class PlayerState(
    val lesson: Lesson? = null,
    val playback: PlaybackState = PlaybackState(),
)

sealed interface PlayerAction {
    data object Start : PlayerAction
    data object PlayPause : PlayerAction
    data class Seek(val positionMs: Long) : PlayerAction
    data object BackClicked : PlayerAction
}

sealed interface PlayerEvent {
    data object NavigateBack : PlayerEvent
}
