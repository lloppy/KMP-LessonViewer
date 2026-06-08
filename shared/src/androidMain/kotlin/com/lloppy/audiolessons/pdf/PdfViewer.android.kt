package com.lloppy.audiolessons.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun PdfViewer(file: PlatformFile, modifier: Modifier) {
    val context = LocalContext.current
    val result by produceState<Result<AndroidPdf>?>(null, file) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val pfd = openDescriptor(context, file)
                AndroidPdf(PdfRenderer(pfd), pfd)
            }
        }
    }
    DisposableEffect(result) {
        onDispose { result?.getOrNull()?.close() }
    }

    val current = result
    when {
        current == null -> Box(modifier, Alignment.Center) { CircularProgressIndicator() }

        current.isFailure -> Box(modifier, Alignment.Center) {
            Text(
                "Не удалось открыть PDF: ${current.exceptionOrNull()?.message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(24.dp),
            )
        }

        else -> {
            val pdf = current.getOrThrow()
            LazyColumn(modifier) {
                items(pdf.pageCount) { index ->
                    val image by produceState<ImageBitmap?>(null, index, pdf) {
                        value = withContext(Dispatchers.IO) { runCatching { pdf.renderPage(index) }.getOrNull() }
                    }
                    val bitmap = image
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.707f))
                    }
                }
            }
        }
    }
}

private suspend fun openDescriptor(context: Context, file: PlatformFile): ParcelFileDescriptor {
    val fromUri = runCatching {
        context.contentResolver.openFileDescriptor(Uri.parse(file.path), "r")
    }.getOrNull()
    if (fromUri != null) return fromUri

    val temp = File(context.cacheDir, "pdf_${file.path.hashCode()}.pdf")
    temp.writeBytes(file.readBytes())
    return ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY)
}

private class AndroidPdf(
    private val renderer: PdfRenderer,
    private val pfd: ParcelFileDescriptor,
) {
    private val mutex = Mutex()
    val pageCount: Int get() = renderer.pageCount

    suspend fun renderPage(index: Int): ImageBitmap = mutex.withLock {
        val page = renderer.openPage(index)
        val scale = 2
        val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap.asImageBitmap()
    }

    fun close() {
        renderer.close()
        pfd.close()
    }
}
