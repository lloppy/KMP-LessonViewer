package com.lloppy.akta

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lloppy.akta.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Akta",
        ) {
            App()
        }
    }
}
