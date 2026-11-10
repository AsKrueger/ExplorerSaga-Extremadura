package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.AppSettings
import com.alonso.explorersaga.data.SettingsStore
import com.alonso.explorersaga.data.StorageManager
import com.alonso.explorersaga.data.StorageUsage
import com.alonso.explorersaga.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface StorageEvent {
    data class CacheCleared(val freedBytes: Long) : StorageEvent
    data object DataCleared : StorageEvent
}

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val storageManager: StorageManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val storageUsage: StateFlow<StorageUsage> = storageManager.usage

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    // Último resultado de una acción de almacenamiento, para mostrarlo una vez en un aviso
    private val _event = MutableStateFlow<StorageEvent?>(null)
    val event: StateFlow<StorageEvent?> = _event.asStateFlow()

    fun refreshStorage() {
        viewModelScope.launch { storageManager.refresh() }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun setOfflineImages(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setOfflineImages(enabled)
            // Desactivarlo significa no guardar imágenes: se borran también las ya guardadas
            if (!enabled) storageManager.clearImages()
        }
    }

    fun clearCache() = runStorageAction {
        StorageEvent.CacheCleared(storageManager.clearCache())
    }

    fun clearSavedData() = runStorageAction {
        storageManager.clearSavedData()
        StorageEvent.DataCleared
    }

    fun consumeEvent() {
        _event.value = null
    }

    private fun runStorageAction(action: suspend () -> StorageEvent) {
        if (_isWorking.value) return
        viewModelScope.launch {
            _isWorking.value = true
            try {
                _event.value = action()
            } finally {
                _isWorking.value = false
            }
        }
    }
}
