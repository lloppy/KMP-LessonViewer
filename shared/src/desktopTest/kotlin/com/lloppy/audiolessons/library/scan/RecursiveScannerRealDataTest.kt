package com.lloppy.audiolessons.library.scan

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Проверка рекурсивного сканера на реальном курсе из Загрузок.
 * Пропускается, если папки нет (например, в CI).
 */
class RecursiveScannerRealDataTest {

    private val root = File(
        System.getProperty("user.home"),
        "Downloads/langme-audiokursispanskogoyazyadlyanachynayuschyhsnulya",
    )

    @Test
    fun everyMp3GetsPdfMatchedByLessonNumber() = runBlocking {
        if (!root.exists()) {
            println("SKIP: пример курса не найден: $root")
            return@runBlocking
        }
        val library = RecursiveLibraryScanner().scan(PlatformFile(root.parentFile))
        val course = library.courses.firstOrNull { it.title.contains("langme") }
        assertTrue(course != null, "Курс langme не найден среди ${library.courses.map { it.title }}")

        val total = course.lessons.size
        val withPdf = course.lessons.count { it.pdf != null }
        println("Курс: ${course.title} — уроков: $total, с PDF: $withPdf")

        assertTrue(total > 100, "Ожидалось много уроков, получено $total")
        assertTrue(withPdf == total, "Не у всех уроков подобран PDF: ${total - withPdf} без PDF")
    }
}
