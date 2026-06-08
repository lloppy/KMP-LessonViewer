package com.lloppy.audiolessons.audio

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class JavaFxAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var player: MediaPlayer? = null

    init {
        ensureToolkit()
    }

    override fun load(mediaId: String, file: PlatformFile, startPositionMs: Long) {
        Platform.runLater {
            player?.dispose()
            _state.value = PlaybackState(mediaId = mediaId, isBuffering = true)
            val media = Media(File(file.path).toURI().toString())
            player = MediaPlayer(media).apply {
                setOnReady {
                    _state.update {
                        it.copy(
                            mediaId = mediaId,
                            durationMs = media.duration.toMillis().toLong(),
                            isBuffering = false,
                        )
                    }
                    if (startPositionMs > 0) seek(Duration.millis(startPositionMs.toDouble()))
                }
                currentTimeProperty().addListener { _, _, t ->
                    _state.update { it.copy(positionMs = t.toMillis().toLong()) }
                }
                setOnEndOfMedia {
                    _state.update { it.copy(isPlaying = false) }
                }
            }
        }
    }

    override fun play() = Platform.runLater {
        player?.play()
        _state.update { it.copy(isPlaying = true) }
    }

    override fun pause() = Platform.runLater {
        player?.pause()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun seekTo(positionMs: Long) = Platform.runLater {
        player?.seek(Duration.millis(positionMs.toDouble()))
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun release() = Platform.runLater {
        player?.dispose()
        player = null
    }

    private companion object {
        val started = AtomicBoolean(false)

        fun ensureToolkit() {
            if (started.compareAndSet(false, true)) {
                try {
                    Platform.startup {}
                } catch (_: IllegalStateException) {
                }
                Platform.setImplicitExit(false)
            }
        }
    }
}
