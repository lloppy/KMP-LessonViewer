package com.lloppy.audiolessons.screens.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lloppy.audiolessons.audio.ProgressRepository
import com.lloppy.audiolessons.library.LibraryRepository
import com.lloppy.audiolessons.library.LibraryState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class LessonsViewModel(
    courseId: String,
    repository: LibraryRepository,
    progressRepository: ProgressRepository,
) : ViewModel() {

    val state: StateFlow<LessonsState> = combine(
        repository.state.map { (it as? LibraryState.Ready)?.library?.course(courseId) },
        progressRepository.progress,
        progressRepository.lastLessonId,
    ) { course, progress, lastLessonId ->
        LessonsState(course = course, progress = progress, lastLessonId = lastLessonId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LessonsState())

    private val _events = Channel<LessonsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: LessonsAction) {
        when (action) {
            is LessonsAction.LessonClicked -> _events.trySend(LessonsEvent.OpenPlayer(action.lessonId))
            LessonsAction.BackClicked -> _events.trySend(LessonsEvent.NavigateBack)
        }
    }
}
