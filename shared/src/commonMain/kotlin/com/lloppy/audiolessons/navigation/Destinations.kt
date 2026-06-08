package com.lloppy.audiolessons.navigation

import kotlinx.serialization.Serializable

@Serializable
object CoursesDestination

@Serializable
data class LessonsDestination(val courseId: String)

@Serializable
data class PlayerDestination(val lessonId: String)

@Serializable
object SettingsDestination
