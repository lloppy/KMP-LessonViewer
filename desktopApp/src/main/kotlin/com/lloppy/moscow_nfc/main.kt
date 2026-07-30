package com.lloppy.moscow_nfc

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lloppy.moscow_nfc.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Moscow NFC",
        ) {
            App()
        }
    }
}
