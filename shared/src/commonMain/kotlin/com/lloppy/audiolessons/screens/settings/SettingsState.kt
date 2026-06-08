package com.lloppy.audiolessons.screens.settings

import com.lloppy.audiolessons.settings.ThemeMode
import io.github.vinceglb.filekit.PlatformFile

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentFolder: String? = null,
)

sealed interface SettingsAction {
    data class SetTheme(val mode: ThemeMode) : SettingsAction
    data class FolderSelected(val file: PlatformFile?) : SettingsAction
    data object ResetProgress : SettingsAction
    data object BackClicked : SettingsAction
}

sealed interface SettingsEvent {
    data object NavigateBack : SettingsEvent
}
