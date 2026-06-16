package com.lloppy.audiolessons.story

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lloppy.audiolessons.library.LibraryRepository
import com.lloppy.audiolessons.library.LibraryState
import com.lloppy.audiolessons.pdf.renderPdfPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoryViewModel(
    private val lessonId: String,
    private val pageIndex: Int,
    private val library: LibraryRepository,
    private val recorder: VoiceRecorder,
    private val exporter: StoryExporter,
) : ViewModel() {

    private val _state = MutableStateFlow(StoryState())
    val state: StateFlow<StoryState> = _state.asStateFlow()

    init {
        loadLesson()
        recorder.state
            .onEach { rs ->
                if (_state.value.phase == StoryPhase.Recording) {
                    _state.update { it.copy(elapsedMs = rs.elapsedMs, amplitude = rs.amplitude) }
                }
            }
            .launchIn(viewModelScope)
        exporter.state
            .onEach { es ->
                if (es is ExportState.Exporting) {
                    _state.update { it.copy(exportProgress = es.progress) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadLesson() {
        val ready = (library.state.value as? LibraryState.Ready)?.library ?: return
        val lesson = ready.lesson(lessonId) ?: return
        _state.update { it.copy(lesson = lesson) }
        val pdf = lesson.pdf ?: return
        viewModelScope.launch {
            val image = renderPdfPage(pdf.file, pageIndex)
            _state.update { it.copy(pageImage = image) }
        }
    }

    fun onAction(action: StoryAction) {
        when (action) {
            StoryAction.Record -> startRecording()
            StoryAction.StopRecording -> stopRecording()
            StoryAction.Reset -> reset()
            StoryAction.Share -> share()
            StoryAction.Export -> export()
            StoryAction.StartCrop -> if (_state.value.pageImage != null) _state.update { it.copy(isCropping = true) }
            StoryAction.CancelCrop -> _state.update { it.copy(isCropping = false) }
            is StoryAction.ApplyCrop -> _state.update { it.copy(croppedImage = action.image, isCropping = false) }
        }
    }

    private fun startRecording() {
        _state.update { it.copy(phase = StoryPhase.Recording, elapsedMs = 0L, error = null, recording = null, mp4Path = null) }
        recorder.start()
    }

    private fun stopRecording() {
        val rec = recorder.stop()
        _state.update {
            it.copy(
                phase = if (rec != null) StoryPhase.Recorded else StoryPhase.Idle,
                recording = rec,
                error = if (rec == null) "Запись не удалась" else null,
            )
        }
    }

    private fun export() {
        val current = _state.value
        val rec = current.recording ?: return
        viewModelScope.launch {
            _state.update { it.copy(phase = StoryPhase.Exporting, exportProgress = 0f, error = null) }
            runCatching {
                exporter.export(
                    attachment = current.croppedImage,
                    title = current.lesson?.title ?: "Урок",
                    audioPath = rec.path,
                    durationMs = rec.durationMs,
                )
            }.onSuccess { path ->
                _state.update { it.copy(phase = StoryPhase.Done, mp4Path = path) }
            }.onFailure { e ->
                _state.update { it.copy(phase = StoryPhase.Recorded, error = e.message ?: "Ошибка экспорта") }
            }
        }
    }

    private fun share() {
        _state.value.mp4Path?.let { exporter.share(it) }
    }

    private fun reset() {
        recorder.cancel()
        _state.update { it.copy(phase = StoryPhase.Idle, recording = null, mp4Path = null, elapsedMs = 0L, error = null) }
    }

    override fun onCleared() {
        recorder.release()
    }
}

sealed interface StoryAction {
    data object Record : StoryAction
    data object StopRecording : StoryAction
    data object Export : StoryAction
    data object Share : StoryAction
    data object Reset : StoryAction
    data object StartCrop : StoryAction
    data object CancelCrop : StoryAction
    data class ApplyCrop(val image: ImageBitmap) : StoryAction
}
