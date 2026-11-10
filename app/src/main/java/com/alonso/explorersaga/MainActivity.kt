package com.alonso.explorersaga

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonso.explorersaga.data.AppSettings
import com.alonso.explorersaga.data.ThemeMode
import com.alonso.explorersaga.navigation.ExplorerSagaRoot
import com.alonso.explorersaga.ui.theme.ExplorerSagaTheme

// AppCompatActivity es necesaria para cambiar el idioma de la app en versiones anteriores a Android 13
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ExplorerSagaApplication).container
        setContent {
            // null hasta leer los ajustes: así no aparece un instante la bienvenida a quien ya la vio
            val settings: AppSettings? by container.settingsStore.settings
                .collectAsStateWithLifecycle(initialValue = null)
            val darkTheme = when (settings?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // El tema puede no coincidir con el del sistema: los iconos de las barras siguen al de la app
            DisposableEffect(darkTheme) {
                val style = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            ExplorerSagaTheme(darkTheme = darkTheme) {
                settings?.let { ExplorerSagaRoot(container = container, settings = it) }
            }
        }
    }
}
