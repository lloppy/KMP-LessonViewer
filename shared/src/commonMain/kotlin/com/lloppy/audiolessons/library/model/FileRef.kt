package com.lloppy.audiolessons.library.model

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name

data class FileRef(
    val file: PlatformFile,
    val id: String,
) {
    val name: String get() = file.name
}
