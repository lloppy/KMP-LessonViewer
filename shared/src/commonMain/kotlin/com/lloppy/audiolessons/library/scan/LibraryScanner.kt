package com.lloppy.audiolessons.library.scan

import com.lloppy.audiolessons.library.model.Library
import io.github.vinceglb.filekit.PlatformFile

fun interface LibraryScanner {
    suspend fun scan(root: PlatformFile): Library
}
