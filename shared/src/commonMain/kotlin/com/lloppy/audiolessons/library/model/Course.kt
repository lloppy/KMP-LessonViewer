package com.lloppy.audiolessons.library.model

data class Course(
    val id: String,
    val title: String,
    val lessons: List<Lesson>,
)
