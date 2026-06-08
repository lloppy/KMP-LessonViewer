package com.lloppy.audiolessons.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lloppy.audiolessons.audio.AudioPlayer
import com.lloppy.audiolessons.audio.ProgressRepository
import com.lloppy.audiolessons.library.LibraryRepository
import com.lloppy.audiolessons.library.LibraryState
import com.lloppy.audiolessons.library.model.Lesson
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class PlayerViewModel(
    private val lessonId: String,
    private val library: LibraryRepository,
    private val player: AudioPlayer,
    private val progress: ProgressRepository,
) : ViewModel() {

    private val lesson = MutableStateFlow<Lesson?>(null)

    val state: StateFlow<PlayerState> = combine(lesson, player.state) { lesson, playback ->
        PlayerState(lesson = lesson, playback = playback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState())

    private val _events = Channel<PlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.Start -> start()
            PlayerAction.PlayPause -> if (player.state.value.isPlaying) player.pause() else player.play()
            is PlayerAction.Seek -> player.seekTo(action.positionMs)
            PlayerAction.BackClicked -> _events.trySend(PlayerEvent.NavigateBack)
        }
    }

    @OptIn(FlowPreview::class)
    private fun start() {
        val resolved = (library.state.value as? LibraryState.Ready)?.library?.lesson(lessonId)
        lesson.value = resolved
        if (resolved != null && player.state.value.mediaId != lessonId) {
            progress.setLastLesson(lessonId)
            player.load(lessonId, resolved.audio.file, progress.get(lessonId).positionMs)
        }
        player.state
            .filter { it.mediaId == lessonId }
            .map { it.positionMs to it.durationMs }
            .debounce(2_000)
            .distinctUntilChanged()
            .onEach { (pos, dur) -> if (pos > 0) progress.save(lessonId, pos, dur) }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        player.pause()
        val playback = player.state.value
        if (playback.mediaId == lessonId && playback.positionMs > 0) {
            progress.save(lessonId, playback.positionMs, playback.durationMs)
        }
    }
}
