package com.lloppy.audiolessons.library.model

data class Lesson(
    val id: String,
    val title: String,
    val audio: FileRef,
    val pdf: FileRef?,
    val section: String? = null,
)
