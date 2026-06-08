package com.lloppy.audiolessons.pdf

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

@Composable
actual fun PdfViewer(file: PlatformFile, modifier: Modifier) {
    val result by produceState<Result<DesktopPdfDocument>?>(null, file) {
        value = runCatching { DesktopPdfDocument.open(file.readBytes()) }
    }

    when (val current = result) {
        null -> Box(modifier, Alignment.Center) { CircularProgressIndicator() }

        else -> {
            val pdf = current.getOrNull()
            when {
                pdf == null -> Box(modifier, Alignment.Center) {
                    Text(
                        "Не удалось открыть PDF: ${current.exceptionOrNull()?.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp),
                    )
                }

                pdf.pageCount == 0 -> Box(modifier, Alignment.Center) {
                    Text("PDF открылся, но в нём 0 страниц", color = MaterialTheme.colorScheme.error)
                }

                else -> PdfPages(pdf, modifier)
            }
        }
    }
}

@Composable
private fun PdfPages(pdf: DesktopPdfDocument, modifier: Modifier) {
    DisposableEffect(pdf) {
        onDispose { pdf.close() }
    }
    LazyColumn(modifier) {
        items(pdf.pageCount) { index ->
            val page by produceState<Result<ImageBitmap>?>(null, index, pdf) {
                value = runCatching { pdf.renderPage(index) }
            }
            when (val rendered = page) {
                null -> Box(Modifier.fillMaxWidth().aspectRatio(0.707f))
                else -> if (rendered.isSuccess) {
                    Image(
                        bitmap = rendered.getOrThrow(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                } else {
                    Text(
                        "Стр. ${index + 1}: ${rendered.exceptionOrNull()?.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
