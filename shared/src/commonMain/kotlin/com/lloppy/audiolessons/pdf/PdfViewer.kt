package com.lloppy.audiolessons.pdf

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
expect fun PdfViewer(file: PlatformFile, modifier: Modifier = Modifier, zoom: Float = 1f)
