package com.lloppy.audiolessons.library.model

data class Library(
    val rootName: String,
    val courses: List<Course>,
) {
    fun course(courseId: String): Course? = courses.firstOrNull { it.id == courseId }

    fun lesson(lessonId: String): Lesson? =
        courses.firstNotNullOfOrNull { course -> course.lessons.firstOrNull { it.id == lessonId } }

    /** Предыдущий и следующий уроки в рамках того же курса (null, если урок крайний). */
    fun neighbors(lessonId: String): Pair<Lesson?, Lesson?> {
        val course = courses.firstOrNull { c -> c.lessons.any { it.id == lessonId } }
            ?: return null to null
        val index = course.lessons.indexOfFirst { it.id == lessonId }
        return course.lessons.getOrNull(index - 1) to course.lessons.getOrNull(index + 1)
    }
}
