// Raíz de la app · Application: crea el contenedor de dependencias una sola vez.
package com.sorbo.app

import android.app.Application

class SorboApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
