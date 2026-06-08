package com.lloppy.audiolessons.audio

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
class AvAudioPlayer : AudioPlayer {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var player: AVPlayer? = null
    private var currentItem: AVPlayerItem? = null
    private var timeObserver: Any? = null

    init {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, null)
        session.setActive(true, null)
    }

    override fun load(mediaId: String, file: PlatformFile, startPositionMs: Long) {
        removeObserver()
        val url = NSURL.fileURLWithPath(file.path)
        val item = AVPlayerItem(uRL = url)
        currentItem = item
        val p = AVPlayer(playerItem = item)
        player = p
        _state.value = PlaybackState(mediaId = mediaId, positionMs = startPositionMs)

        val interval = CMTimeMakeWithSeconds(0.5, 1000)
        timeObserver = p.addPeriodicTimeObserverForInterval(interval, null) { time ->
            val positionMs = (CMTimeGetSeconds(time) * 1000).toLong()
            val durationSec = CMTimeGetSeconds(item.duration)
            val durationMs = if (durationSec.isNaN()) 0L else (durationSec * 1000).toLong()
            _state.update { it.copy(positionMs = positionMs, durationMs = durationMs) }
        }
        if (startPositionMs > 0) seekTo(startPositionMs)
    }

    override fun play() {
        player?.play()
        _state.update { it.copy(isPlaying = true) }
    }

    override fun pause() {
        player?.pause()
        _state.update { it.copy(isPlaying = false) }
    }

    override fun seekTo(positionMs: Long) {
        player?.seekToTime(CMTimeMakeWithSeconds(positionMs / 1000.0, 1000))
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun release() {
        removeObserver()
        player?.pause()
        player = null
        currentItem = null
        AVAudioSession.sharedInstance().setActive(false, null)
    }

    private fun removeObserver() {
        timeObserver?.let { player?.removeTimeObserver(it) }
        timeObserver = null
    }
}
