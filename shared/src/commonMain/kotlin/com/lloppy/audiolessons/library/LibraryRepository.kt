package com.lloppy.audiolessons.library

import com.lloppy.audiolessons.library.model.Library
import com.lloppy.audiolessons.library.scan.LibraryScanner
import com.russhwolf.settings.Settings
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.fromBookmarkData
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

sealed interface LibraryState {
    data object Loading : LibraryState
    data object NoRoot : LibraryState
    data class Ready(val library: Library) : LibraryState
    data class Error(val message: String) : LibraryState
}

@OptIn(ExperimentalEncodingApi::class)
class LibraryRepository(
    private val settings: Settings,
    private val scanner: LibraryScanner,
) {
    private val _state = MutableStateFlow<LibraryState>(LibraryState.Loading)
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val _currentRootName = MutableStateFlow<String?>(null)
    val currentRootName: StateFlow<String?> = _currentRootName.asStateFlow()

    private var root: PlatformFile? = null
    private var initialized = false

    suspend fun ensureLoaded() {
        if (initialized) return
        initialized = true
        restore()
    }

    suspend fun setRoot(picked: PlatformFile) {
        initialized = true
        withContext(Dispatchers.Default) {
            runCatching {
                val bytes = picked.bookmarkData().bytes
                settings.putString(KEY_ROOT_BOOKMARK, Base64.encode(bytes))
            }
        }
        root = picked
        rescan(picked)
    }

    suspend fun restore() {
        val stored = settings.getStringOrNull(KEY_ROOT_BOOKMARK)
        if (stored == null) {
            _state.value = LibraryState.NoRoot
            return
        }
        val restored = withContext(Dispatchers.Default) {
            runCatching {
                PlatformFile.fromBookmarkData(Base64.decode(stored)).takeIf { it.exists() }
            }.getOrNull()
        }
        if (restored == null) {
            settings.remove(KEY_ROOT_BOOKMARK)
            _state.value = LibraryState.NoRoot
            return
        }
        root = restored
        rescan(restored)
    }

    suspend fun refresh() {
        root?.let { rescan(it) }
    }

    fun clearRoot() {
        settings.remove(KEY_ROOT_BOOKMARK)
        root = null
        _currentRootName.value = null
        _state.value = LibraryState.NoRoot
    }

    private suspend fun rescan(rootFile: PlatformFile) {
        _state.value = LibraryState.Loading
        _state.value = try {
            val library = withContext(Dispatchers.Default) {
                _currentRootName.value = rootFile.name
                scanner.scan(rootFile)
            }
            LibraryState.Ready(library)
        } catch (t: Throwable) {
            LibraryState.Error(t.message ?: "Не удалось прочитать папку")
        }
    }

    private companion object {
        const val KEY_ROOT_BOOKMARK = "library.root.bookmark"
    }
}
