package com.lloppy.audiolessons

import com.lloppy.audiolessons.settings.ThemeMode

data class AppState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
