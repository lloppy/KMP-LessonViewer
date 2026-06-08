package com.lloppy.audiolessons.screens.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lloppy.audiolessons.library.LibraryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoursesViewModel(
    private val repository: LibraryRepository,
) : ViewModel() {

    val state: StateFlow<CoursesState> = repository.state
        .map { CoursesState(content = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoursesState())

    private val _events = Channel<CoursesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { repository.ensureLoaded() }
    }

    fun onAction(action: CoursesAction) {
        when (action) {
            is CoursesAction.FolderSelected ->
                action.file?.let { file -> viewModelScope.launch { repository.setRoot(file) } }

            is CoursesAction.CourseClicked -> _events.trySend(CoursesEvent.OpenLessons(action.courseId))
            CoursesAction.SettingsClicked -> _events.trySend(CoursesEvent.OpenSettings)
        }
    }
}
