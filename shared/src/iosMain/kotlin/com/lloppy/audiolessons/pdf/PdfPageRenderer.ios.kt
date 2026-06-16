package com.lloppy.audiolessons.pdf

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun renderPdfPage(file: PlatformFile, pageIndex: Int): ImageBitmap? = null
