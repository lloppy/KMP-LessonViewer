package com.lloppy.audiolessons.audio

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Media3AudioPlayer(context: Context) : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var controller: MediaController? = null
    private var pollJob: Job? = null

    init {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get().also { c ->
                c.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _state.update { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) startPolling() else stopPolling()
                        pushPosition()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _state.update { it.copy(isBuffering = playbackState == Player.STATE_BUFFERING) }
                        pushPosition()
                    }
                })
            }
        }, MoreExecutors.directExecutor())
    }

    override fun load(mediaId: String, file: PlatformFile, startPositionMs: Long) {
        val item = MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(file.path)
            .build()
        controller?.run {
            setMediaItem(item, startPositionMs)
            prepare()
        }
        _state.update { it.copy(mediaId = mediaId, positionMs = startPositionMs) }
    }

    override fun play() {
        controller?.play()
    }

    override fun pause() {
        controller?.pause()
        pushPosition()
    }

    override fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun release() {
        stopPolling()
        controller?.release()
        controller = null
        scope.cancel()
    }

    private fun startPolling() {
        stopPolling()
        pollJob = scope.launch {
            while (isActive) {
                pushPosition()
                delay(500)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun pushPosition() {
        val c = controller ?: return
        val duration = c.duration.let { if (it == C.TIME_UNSET) 0L else it }
        _state.update { it.copy(positionMs = c.currentPosition, durationMs = duration) }
    }
}
