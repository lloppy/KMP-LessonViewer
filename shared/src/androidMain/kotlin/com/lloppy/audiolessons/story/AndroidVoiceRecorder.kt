package com.lloppy.audiolessons.story

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AndroidVoiceRecorder(context: Context) : VoiceRecorder {

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(RecordingState())
    override val state: StateFlow<RecordingState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt = 0L
    private var tickJob: Job? = null

    override fun start() {
        if (recorder != null) return
        val dir = File(appContext.cacheDir, "stories").apply { mkdirs() }
        val file = File(dir, "voice_${SystemClock.elapsedRealtime()}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setAudioEncodingBitRate(128_000)
        rec.setAudioSamplingRate(44_100)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()

        recorder = rec
        outputFile = file
        startedAt = SystemClock.elapsedRealtime()
        _state.value = RecordingState(isRecording = true)
        tickJob = scope.launch {
            while (isActive) {
                val r = recorder ?: break
                val elapsed = SystemClock.elapsedRealtime() - startedAt
                val amp = runCatching { r.maxAmplitude }.getOrDefault(0)
                _state.value = RecordingState(true, elapsed, (amp / 32767f).coerceIn(0f, 1f))
                delay(100)
            }
        }
    }

    override fun stop(): Recording? {
        val r = recorder ?: return null
        val file = outputFile
        val elapsed = SystemClock.elapsedRealtime() - startedAt
        tickJob?.cancel()
        val ok = runCatching { r.stop() }.isSuccess
        runCatching { r.release() }
        recorder = null
        _state.value = RecordingState(isRecording = false, elapsedMs = elapsed)
        return if (ok && file != null) Recording(file.absolutePath, elapsed) else null
    }

    override fun cancel() {
        tickJob?.cancel()
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        outputFile?.delete()
        outputFile = null
        _state.value = RecordingState()
    }

    override fun release() {
        tickJob?.cancel()
        runCatching { recorder?.release() }
        recorder = null
        scope.cancel()
    }
}
