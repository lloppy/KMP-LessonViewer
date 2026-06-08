package com.lloppy.audiolessons.pdf

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PdfViewer(file: PlatformFile, modifier: Modifier) {
    UIKitView(
        factory = {
            PDFView().apply {
                autoScales = true
                document = PDFDocument(uRL = NSURL.fileURLWithPath(file.path))
            }
        },
        modifier = modifier,
    )
}
