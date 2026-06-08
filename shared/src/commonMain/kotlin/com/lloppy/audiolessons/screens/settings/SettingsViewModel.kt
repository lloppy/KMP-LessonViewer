package com.lloppy.audiolessons.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lloppy.audiolessons.audio.ProgressRepository
import com.lloppy.audiolessons.library.LibraryRepository
import com.lloppy.audiolessons.library.LibraryState
import com.lloppy.audiolessons.settings.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val library: LibraryRepository,
    private val progress: ProgressRepository,
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(settings.themeMode, library.state) { mode, libraryState ->
        SettingsState(
            themeMode = mode,
            currentFolder = (libraryState as? LibraryState.Ready)?.library?.rootName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetTheme -> settings.setThemeMode(action.mode)
            is SettingsAction.FolderSelected ->
                action.file?.let { file -> viewModelScope.launch { library.setRoot(file) } }

            SettingsAction.ResetProgress -> progress.clearAll()
            SettingsAction.BackClicked -> _events.trySend(SettingsEvent.NavigateBack)
        }
    }
}
