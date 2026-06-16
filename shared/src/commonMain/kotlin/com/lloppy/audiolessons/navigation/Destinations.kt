package com.lloppy.audiolessons.navigation

import kotlinx.serialization.Serializable

@Serializable
object CoursesDestination

@Serializable
data class LessonsDestination(val courseId: String)

@Serializable
data class PlayerDestination(val lessonId: String)

@Serializable
data class StoryDestination(val lessonId: String, val pageIndex: Int = 0)

@Serializable
object SettingsDestination
