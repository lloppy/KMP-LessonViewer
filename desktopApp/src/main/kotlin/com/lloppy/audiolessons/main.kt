package com.lloppy.audiolessons

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lloppy.audiolessons.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Аудиоуроки",
        ) {
            App()
        }
    }
}
