package com.lloppy.audiolessons.screens.lessons

import com.lloppy.audiolessons.audio.LessonProgress
import com.lloppy.audiolessons.library.model.Course

data class LessonsState(
    val course: Course? = null,
    val progress: Map<String, LessonProgress> = emptyMap(),
    val lastLessonId: String? = null,
)

sealed interface LessonsAction {
    data class LessonClicked(val lessonId: String) : LessonsAction
    data object BackClicked : LessonsAction
}

sealed interface LessonsEvent {
    data class OpenPlayer(val lessonId: String) : LessonsEvent
    data object NavigateBack : LessonsEvent
}
