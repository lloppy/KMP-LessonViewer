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
    lessonId: String,
    private val library: LibraryRepository,
    private val player: AudioPlayer,
    private val progress: ProgressRepository,
) : ViewModel() {

    private var currentId: String = lessonId

    private val lesson = MutableStateFlow<Lesson?>(null)
    private val neighbors = MutableStateFlow<Pair<Lesson?, Lesson?>>(null to null)

    val state: StateFlow<PlayerState> =
        combine(lesson, player.state, neighbors) { lesson, playback, (prev, next) ->
            PlayerState(
                lesson = lesson,
                playback = playback,
                hasPrev = prev != null,
                hasNext = next != null,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState())

    private val _events = Channel<PlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.Start -> start()
            PlayerAction.PlayPause -> if (player.state.value.isPlaying) player.pause() else player.play()
            is PlayerAction.Seek -> player.seekTo(action.positionMs)
            is PlayerAction.Skip -> skip(action.deltaMs)
            PlayerAction.PrevLesson -> neighbors.value.first?.let { loadLesson(it.id, autoPlay = true) }
            PlayerAction.NextLesson -> neighbors.value.second?.let { loadLesson(it.id, autoPlay = true) }
            PlayerAction.BackClicked -> _events.trySend(PlayerEvent.NavigateBack)
        }
    }

    @OptIn(FlowPreview::class)
    private fun start() {
        loadLesson(currentId, autoPlay = false)
        player.state
            .filter { it.mediaId != null && it.positionMs > 0 }
            .map { Triple(it.mediaId!!, it.positionMs, it.durationMs) }
            .debounce(2_000)
            .distinctUntilChanged()
            .onEach { (id, pos, dur) -> progress.save(id, pos, dur) }
            .launchIn(viewModelScope)
    }

    private fun loadLesson(id: String, autoPlay: Boolean) {
        val ready = (library.state.value as? LibraryState.Ready)?.library ?: return
        val resolved = ready.lesson(id) ?: return
        currentId = id
        lesson.value = resolved
        neighbors.value = ready.neighbors(id)
        if (player.state.value.mediaId != id) {
            progress.setLastLesson(id)
            player.load(id, resolved.audio.file, progress.get(id).positionMs)
        }
        if (autoPlay) player.play()
    }

    private fun skip(deltaMs: Long) {
        val playback = player.state.value
        val max = if (playback.durationMs > 0) playback.durationMs else Long.MAX_VALUE
        val target = (playback.positionMs + deltaMs).coerceIn(0L, max)
        player.seekTo(target)
    }

    override fun onCleared() {
        player.pause()
        val playback = player.state.value
        val id = playback.mediaId
        if (id != null && playback.positionMs > 0) {
            progress.save(id, playback.positionMs, playback.durationMs)
        }
    }
}
