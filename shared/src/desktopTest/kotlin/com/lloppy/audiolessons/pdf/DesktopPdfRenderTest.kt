package com.lloppy.audiolessons.pdf

import com.lloppy.audiolessons.library.scan.RecursiveLibraryScanner
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopPdfRenderTest {

    private val root = File(
        System.getProperty("user.home"),
        "Downloads/langme-audiokursispanskogoyazyadlyanachynayuschyhsnulya",
    )

    @Test
    fun rendersAllPagesConcurrentlyWithoutErrors() = runBlocking {
        if (!root.exists()) {
            println("SKIP: пример курса не найден: $root")
            return@runBlocking
        }
        val library = RecursiveLibraryScanner().scan(PlatformFile(root.parentFile))
        val course = library.courses.first { it.title.contains("langme") }
        val lesson = course.lessons.first { it.pdf != null }
        val pdf = lesson.pdf!!.file
        println("lesson=${lesson.title} pdf=${pdf.name}")

        val document = DesktopPdfDocument.open(pdf.readBytes())
        println("pages=${document.pageCount}")

        val outcome = runCatching {
            (0 until document.pageCount)
                .map { index -> async { document.renderPage(index) } }
                .awaitAll()
                .map { it.width to it.height }
        }
        document.close()

        println("concurrent render outcome = ${outcome.map { it.size }}")
        assertTrue(outcome.isSuccess, "Конкурентный рендер упал: ${outcome.exceptionOrNull()}")
        assertTrue(document.pageCount > 0, "0 страниц")
    }
}
