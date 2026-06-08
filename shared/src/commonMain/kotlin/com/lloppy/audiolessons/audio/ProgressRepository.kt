package com.lloppy.audiolessons.audio

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LessonProgress(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
) {
    val fraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

class ProgressRepository(
    private val settings: Settings,
) {
    private val _progress = MutableStateFlow(loadAll())
    val progress: StateFlow<Map<String, LessonProgress>> = _progress.asStateFlow()

    private val _lastLessonId = MutableStateFlow(settings.getStringOrNull(KEY_LAST))
    val lastLessonId: StateFlow<String?> = _lastLessonId.asStateFlow()

    fun get(lessonId: String): LessonProgress = _progress.value[lessonId] ?: LessonProgress()

    fun save(lessonId: String, positionMs: Long, durationMs: Long) {
        val updated = _progress.value + (lessonId to LessonProgress(positionMs, durationMs))
        _progress.value = updated
        settings.putString(KEY_MAP, Json.encodeToString(updated))
    }

    fun setLastLesson(lessonId: String) {
        _lastLessonId.value = lessonId
        settings.putString(KEY_LAST, lessonId)
    }

    fun clearAll() {
        _progress.value = emptyMap()
        settings.remove(KEY_MAP)
        _lastLessonId.value = null
        settings.remove(KEY_LAST)
    }

    private fun loadAll(): Map<String, LessonProgress> {
        val raw = settings.getStringOrNull(KEY_MAP) ?: return emptyMap()
        return runCatching { Json.decodeFromString<Map<String, LessonProgress>>(raw) }.getOrDefault(emptyMap())
    }

    private companion object {
        const val KEY_MAP = "audio.progress.map"
        const val KEY_LAST = "audio.lastLesson"
    }
}
