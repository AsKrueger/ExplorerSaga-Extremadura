package com.alonso.explorersaga.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Activado por defecto: quien viaja por Extremadura pasa por zonas sin cobertura
    val offlineImages: Boolean = true,
    val welcomeSeen: Boolean = false
)

private val Context.dataStore by preferencesDataStore("settings")

// El idioma no se guarda aquí: lo guarda Android (idioma por app) a través de AppCompatDelegate
class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<AppSettings> = store.data.map { preferences ->
        AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[THEME_MODE] } ?: ThemeMode.SYSTEM,
            offlineImages = preferences[OFFLINE_IMAGES] ?: true,
            welcomeSeen = preferences[WELCOME_SEEN] ?: false
        )
    }

    suspend fun setThemeMode(value: ThemeMode) {
        store.edit { it[THEME_MODE] = value.name }
    }

    suspend fun setOfflineImages(value: Boolean) {
        store.edit { it[OFFLINE_IMAGES] = value }
    }

    suspend fun setWelcomeSeen(value: Boolean) {
        store.edit { it[WELCOME_SEEN] = value }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val OFFLINE_IMAGES = booleanPreferencesKey("offline_images")
        val WELCOME_SEEN = booleanPreferencesKey("welcome_seen")
    }
}
