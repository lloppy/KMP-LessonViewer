package com.lloppy.audiolessons.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min
import kotlin.math.sqrt

private const val SCALE = 2
private const val GAP = 16
private const val PIXEL_BUDGET = 16_000_000.0

actual suspend fun renderPdfDocument(file: PlatformFile): ImageBitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val temp = File.createTempFile("story_pdf_", ".pdf")
            temp.writeBytes(file.readBytes())
            val pfd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount

            val sizes = (0 until pageCount).map { index ->
                val page = renderer.openPage(index)
                val size = (page.width * SCALE) to (page.height * SCALE)
                page.close()
                size
            }

            val rawW = sizes.maxOfOrNull { it.first } ?: 1
            val rawH = sizes.sumOf { it.second } + GAP * SCALE * (pageCount - 1).coerceAtLeast(0)
            val factor = min(1.0, sqrt(PIXEL_BUDGET / (rawW.toDouble() * rawH))).toFloat()

            val docW = (rawW * factor).toInt().coerceAtLeast(1)
            val docH = (rawH * factor).toInt().coerceAtLeast(1)
            val doc = Bitmap.createBitmap(docW, docH, Bitmap.Config.ARGB_8888)
            doc.eraseColor(Color.WHITE)
            val canvas = Canvas(doc)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)

            var y = 0f
            for (index in 0 until pageCount) {
                val (pw, ph) = sizes[index]
                val page = renderer.openPage(index)
                val pageBmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
                pageBmp.eraseColor(Color.WHITE)
                page.render(pageBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val dw = pw * factor
                val dh = ph * factor
                val x = (docW - dw) / 2f
                canvas.drawBitmap(pageBmp, Rect(0, 0, pw, ph), RectF(x, y, x + dw, y + dh), paint)
                pageBmp.recycle()
                y += dh + GAP * SCALE * factor
            }

            renderer.close()
            pfd.close()
            temp.delete()
            doc.asImageBitmap()
        }.getOrNull()
    }
