package com.lloppy.audiolessons.library.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NaturalOrderTest {

    /** «Урок 8» эквивалентен «Урок 8.0» и должен идти перед «Урок 8.1». */
    @Test
    fun lessonWithoutDecimalGoesBeforeSubLesson() {
        assertTrue(naturalCompare("Урок 8", "Урок 8.1") < 0)
        assertTrue(naturalCompare("Урок 8", "Урок 8.0") <= 0)
        assertTrue(naturalCompare("Урок 8.0", "Урок 8.1") < 0)
    }

    /** Сортировка по базовому имени (без .mp3) сохраняет правильный порядок. */
    @Test
    fun sortsLessonBasesNaturally() {
        val sorted = listOf("Урок 8.1", "Урок 10", "Урок 8", "Урок 8.2", "Урок 9")
            .sortedWith(NaturalOrder)
        assertEquals(
            listOf("Урок 8", "Урок 8.1", "Урок 8.2", "Урок 9", "Урок 10"),
            sorted,
        )
    }
}
