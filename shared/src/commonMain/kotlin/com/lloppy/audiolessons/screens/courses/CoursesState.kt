package com.lloppy.audiolessons.screens.courses

import com.lloppy.audiolessons.library.LibraryState
import io.github.vinceglb.filekit.PlatformFile

data class CoursesState(
    val content: LibraryState = LibraryState.Loading,
)

sealed interface CoursesAction {
    data class FolderSelected(val file: PlatformFile?) : CoursesAction
    data class CourseClicked(val courseId: String) : CoursesAction
    data object SettingsClicked : CoursesAction
}

sealed interface CoursesEvent {
    data class OpenLessons(val courseId: String) : CoursesEvent
    data object OpenSettings : CoursesEvent
}
