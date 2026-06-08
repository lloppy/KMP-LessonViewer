package com.lloppy.audiolessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lloppy.audiolessons.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<AppState> = settings.themeMode
        .map { AppState(themeMode = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState())
}
