package com.lloppy.audiolessons.library.model

data class Library(
    val rootName: String,
    val courses: List<Course>,
) {
    fun course(courseId: String): Course? = courses.firstOrNull { it.id == courseId }

    fun lesson(lessonId: String): Lesson? =
        courses.firstNotNullOfOrNull { course -> course.lessons.firstOrNull { it.id == lessonId } }
}
