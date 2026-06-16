package com.lloppy.audiolessons.library.scan

import com.lloppy.audiolessons.library.model.Course
import com.lloppy.audiolessons.library.model.FileRef
import com.lloppy.audiolessons.library.model.Lesson
import com.lloppy.audiolessons.library.model.Library
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class RecursiveLibraryScanner : LibraryScanner {

    private class Entry(
        val file: PlatformFile,
        val name: String,
        val base: String,
        val ext: String,
        val isDir: Boolean,
    )

    override suspend fun scan(root: PlatformFile): Library = coroutineScope {
        val dirs = entries(root).filter { it.isDir }
        val courses = dirs
            .map { dir -> async { scanCourse(dir.file, dir.name) } }
            .awaitAll()
            .filter { it.lessons.isNotEmpty() }
            .sortedWith(compareBy(NaturalOrder) { it.title })
        Library(rootName = root.name, courses = courses)
    }

    private suspend fun scanCourse(courseDir: PlatformFile, courseName: String): Course =
        Course(
            id = courseName,
            title = courseName,
            lessons = walk(courseDir, listOf(courseName), section = null),
        )

    private suspend fun walk(
        dir: PlatformFile,
        idPrefix: List<String>,
        section: String?,
    ): List<Lesson> = coroutineScope {
        val entries = entries(dir)
        val files = entries.filterNot { it.isDir }
        val mp3s = files.filter { it.ext.equals("mp3", ignoreCase = true) }
            .sortedWith(compareBy(NaturalOrder) { it.base })
        val pdfs = files.filter { it.ext.equals("pdf", ignoreCase = true) }

        val pdfByNumber = HashMap<Int, Entry>()
        for (pdf in pdfs) {
            val n = lessonNumber(pdf.base) ?: continue
            if (n !in pdfByNumber) pdfByNumber[n] = pdf
        }
        val singlePdf = pdfs.singleOrNull()

        val here = mp3s.map { mp3 ->
            val pdf = pdfs.firstOrNull { it.base.equals(mp3.base, ignoreCase = true) }
                ?: lessonNumber(mp3.base)?.let { pdfByNumber[it] }
                ?: singlePdf
            val id = (idPrefix + mp3.name).joinToString("/")
            Lesson(
                id = id,
                title = mp3.base,
                audio = FileRef(mp3.file, id),
                pdf = pdf?.let { FileRef(it.file, (idPrefix + it.name).joinToString("/")) },
                section = section,
            )
        }

        val deeper = entries.filter { it.isDir }
            .sortedWith(compareBy(NaturalOrder) { it.name })
            .map { sub -> async { walk(sub.file, idPrefix + sub.name, section = sub.name) } }
            .awaitAll()
            .flatten()

        here + deeper
    }

    /** Читает детей папки и их метаданные параллельно — на SAF каждый доступ это IPC. */
    private suspend fun entries(dir: PlatformFile): List<Entry> = coroutineScope {
        dir.list()
            .map { file ->
                async {
                    Entry(
                        file = file,
                        name = file.name,
                        base = file.nameWithoutExtension,
                        ext = file.extension,
                        isDir = file.isDirectory(),
                    )
                }
            }
            .awaitAll()
    }

    private fun lessonNumber(name: String): Int? =
        LESSON_RE.find(name.lowercase())?.groupValues?.getOrNull(1)?.toIntOrNull()

    private companion object {
        val LESSON_RE = Regex("урок\\s*(\\d+)")
    }
}

internal val NaturalOrder = Comparator<String> { a, b -> naturalCompare(a, b) }

internal fun naturalCompare(a: String, b: String): Int {
    val x = a.lowercase()
    val y = b.lowercase()
    var i = 0
    var j = 0
    while (i < x.length && j < y.length) {
        val cx = x[i]
        val cy = y[j]
        if (cx.isDigit() && cy.isDigit()) {
            var ni = i
            while (ni < x.length && x[ni].isDigit()) ni++
            var nj = j
            while (nj < y.length && y[nj].isDigit()) nj++
            val nx = x.substring(i, ni).trimStart('0').ifEmpty { "0" }
            val ny = y.substring(j, nj).trimStart('0').ifEmpty { "0" }
            if (nx.length != ny.length) return nx.length - ny.length
            val c = nx.compareTo(ny)
            if (c != 0) return c
            i = ni
            j = nj
        } else {
            if (cx != cy) return cx.compareTo(cy)
            i++
            j++
        }
    }
    return (x.length - i) - (y.length - j)
}
