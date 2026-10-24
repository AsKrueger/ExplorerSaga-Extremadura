package com.alonso.explorersaga

import android.app.Application
import java.io.File

/**
 * Custom Application class para inicializar y mantener el AppContainer.
 */
class ExplorerSagaApplication : Application() {
    /**
     * La instancia de AppContainer que usarán el resto de clases para obtener
     * sus dependencias.
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Inicializamos el contenedor cuando la app arranca.
        container = DefaultAppContainer(this)
        // Caché JSON de versiones anteriores, sustituida por Room
        File(filesDir, "saved_places.json").delete()
    }
}
