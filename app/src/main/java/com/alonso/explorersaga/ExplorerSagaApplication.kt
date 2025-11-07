package com.alonso.explorersaga

import android.app.Application

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
    }
}
