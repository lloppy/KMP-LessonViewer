package com.lloppy.audiolessons.library.scan

import com.lloppy.audiolessons.library.model.Course
import com.lloppy.audiolessons.library.model.FileRef
import com.lloppy.audiolessons.library.model.Lesson
import com.lloppy.audiolessons.library.model.Library
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension

class RecursiveLibraryScanner : LibraryScanner {

    override suspend fun scan(root: PlatformFile): Library {
        val courses = root.list()
            .filter { it.isDirectory() }
            .sortedWith(byName)
            .map { scanCourse(it) }
            .filter { it.lessons.isNotEmpty() }
        return Library(rootName = root.name, courses = courses)
    }

    private suspend fun scanCourse(courseDir: PlatformFile): Course {
        val lessons = ArrayList<Lesson>()
        walk(courseDir, idPrefix = listOf(courseDir.name), section = null, out = lessons)
        return Course(id = courseDir.name, title = courseDir.name, lessons = lessons)
    }

    private suspend fun walk(
        dir: PlatformFile,
        idPrefix: List<String>,
        section: String?,
        out: MutableList<Lesson>,
    ) {
        val children = dir.list()
        val files = children.filter { it.isRegularFile() }
        val mp3s = files.filter { it.extension.equals("mp3", ignoreCase = true) }.sortedWith(byName)
        val pdfs = files.filter { it.extension.equals("pdf", ignoreCase = true) }

        val pdfByNumber = HashMap<Int, PlatformFile>()
        for (pdf in pdfs) {
            val n = lessonNumber(pdf.nameWithoutExtension) ?: continue
            if (n !in pdfByNumber) pdfByNumber[n] = pdf
        }
        val singlePdf = pdfs.singleOrNull()

        for (mp3 in mp3s) {
            val base = mp3.nameWithoutExtension
            val pdf = pdfs.firstOrNull { it.nameWithoutExtension.equals(base, ignoreCase = true) }
                ?: lessonNumber(base)?.let { pdfByNumber[it] }
                ?: singlePdf
            val id = (idPrefix + mp3.name).joinToString("/")
            out += Lesson(
                id = id,
                title = base,
                audio = FileRef(mp3, id),
                pdf = pdf?.let { FileRef(it, (idPrefix + it.name).joinToString("/")) },
                section = section,
            )
        }

        children.filter { it.isDirectory() }
            .sortedWith(byName)
            .forEach { sub -> walk(sub, idPrefix + sub.name, section = sub.name, out = out) }
    }

    private fun lessonNumber(name: String): Int? =
        LESSON_RE.find(name.lowercase())?.groupValues?.getOrNull(1)?.toIntOrNull()

    private companion object {
        // Без (?i): Java-regex не сворачивает регистр кириллицы, поэтому имя приводим к lowercase сами.
        val LESSON_RE = Regex("урок\\s*(\\d+)")
        val byName = Comparator<PlatformFile> { a, b -> naturalCompare(a.name, b.name) }
    }
}

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
