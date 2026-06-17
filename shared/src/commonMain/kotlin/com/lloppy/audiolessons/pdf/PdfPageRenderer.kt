package com.lloppy.audiolessons.pdf

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

expect suspend fun renderPdfDocument(file: PlatformFile): ImageBitmap?
