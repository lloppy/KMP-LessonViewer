package com.lloppy.audiolessons

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lloppy.audiolessons.navigation.AppNavHost
import com.lloppy.audiolessons.settings.ThemeMode
import com.lloppy.audiolessons.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val viewModel = koinViewModel<AppViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val darkTheme = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    AppTheme(darkTheme = darkTheme) {
        Surface {
            AppNavHost()
        }
    }
}
