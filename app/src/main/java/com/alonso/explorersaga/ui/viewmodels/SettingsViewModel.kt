package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.AppSettings
import com.alonso.explorersaga.data.OfflineMapManager
import com.alonso.explorersaga.data.OfflineMapState
import com.alonso.explorersaga.data.SettingsStore
import com.alonso.explorersaga.data.StorageManager
import com.alonso.explorersaga.data.StorageUsage
import com.alonso.explorersaga.data.ThemeMode
import com.alonso.explorersaga.data.content.ContentRepository
import com.alonso.explorersaga.data.content.SyncResult
import com.alonso.explorersaga.data.db.ContentMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class CacheCleared(val freedBytes: Long) : SettingsEvent
    data object DataCleared : SettingsEvent
    data class Synced(val result: SyncResult) : SettingsEvent
}

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val storageManager: StorageManager,
    private val contentRepository: ContentRepository,
    private val offlineMap: OfflineMapManager,
    private val scheduleSync: (wifiOnly: Boolean) -> Unit
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val storageUsage: StateFlow<StorageUsage> = storageManager.usage

    val contentMeta: StateFlow<ContentMeta?> = contentRepository.meta
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val offlineMapState: StateFlow<OfflineMapState> = offlineMap.state

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    // Último resultado de una acción, para mostrarlo una vez en un aviso
    private val _event = MutableStateFlow<SettingsEvent?>(null)
    val event: StateFlow<SettingsEvent?> = _event.asStateFlow()

    fun refresh() {
        viewModelScope.launch { storageManager.refresh() }
        offlineMap.refresh()
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

    fun setSyncWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setSyncWifiOnly(enabled)
            scheduleSync(enabled)
        }
    }

    fun syncNow() = runAction { SettingsEvent.Synced(contentRepository.sync()) }

    fun downloadOfflineMap(styleUrls: List<String>, pixelRatio: Float) = offlineMap.download(styleUrls, pixelRatio)

    fun refreshStorage() {
        viewModelScope.launch { storageManager.refresh() }
    }

    fun deleteOfflineMap() {
        viewModelScope.launch {
            offlineMap.delete()
            storageManager.refresh()
        }
    }

    fun clearCache() = runAction { SettingsEvent.CacheCleared(storageManager.clearCache()) }

    fun clearSavedData() = runAction {
        storageManager.clearSavedData()
        SettingsEvent.DataCleared
    }

    fun consumeEvent() {
        _event.value = null
    }

    private fun runAction(action: suspend () -> SettingsEvent) {
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
