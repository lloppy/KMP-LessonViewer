package com.lloppy.audiolessons.pdf

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.util.concurrent.Executors

/**
 * PDFBox требует доступа к документу из одного потока — закрепляем загрузку и рендер
 * за единственным выделенным потоком (сериализация мьютексом поверх пула не спасает).
 */
internal class DesktopPdfDocument private constructor(
    private val executor: java.util.concurrent.ExecutorService,
    private val document: PDDocument,
    private val renderer: PDFRenderer,
) {
    val pageCount: Int = document.numberOfPages

    suspend fun renderPage(index: Int, dpi: Float = 144f): ImageBitmap =
        withContext(executor.asCoroutineDispatcher()) {
            renderer.renderImageWithDPI(index, dpi).toComposeImageBitmap()
        }

    fun close() {
        executor.execute { runCatching { document.close() } }
        executor.shutdown()
    }

    companion object {
        suspend fun open(bytes: ByteArray): DesktopPdfDocument {
            val executor = Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "pdf-render").apply { isDaemon = true }
            }
            return withContext(executor.asCoroutineDispatcher()) {
                val document = Loader.loadPDF(bytes)
                DesktopPdfDocument(executor, document, PDFRenderer(document))
            }
        }
    }
}
