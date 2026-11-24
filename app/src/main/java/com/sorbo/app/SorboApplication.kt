// Raíz de la app · Application: crea el contenedor de dependencias una sola vez y arranca la sincronización.
package com.sorbo.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SorboApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Vive lo que vive la app: respalda los cambios pendientes cuando hay cuenta (I5).
        container.disparadorDeSincronizacion.iniciar(CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }
}
