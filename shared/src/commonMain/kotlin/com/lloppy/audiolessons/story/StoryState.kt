package com.lloppy.audiolessons.story

import androidx.compose.ui.graphics.ImageBitmap
import com.lloppy.audiolessons.library.model.Lesson

enum class StoryPhase { Idle, Recording, Recorded, Exporting, Done }

data class StoryState(
    val lesson: Lesson? = null,
    val pageImage: ImageBitmap? = null,
    val croppedImage: ImageBitmap? = null,
    val isCropping: Boolean = false,
    val phase: StoryPhase = StoryPhase.Idle,
    val elapsedMs: Long = 0L,
    val amplitude: Float = 0f,
    val recording: Recording? = null,
    val exportProgress: Float = 0f,
    val mp4Path: String? = null,
    val error: String? = null,
) {
    val displayDurationMs: Long get() = recording?.durationMs ?: elapsedMs
}
