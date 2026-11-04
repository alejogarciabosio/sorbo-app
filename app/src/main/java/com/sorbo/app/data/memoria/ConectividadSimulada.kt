// Capa de datos · conectividad simulada: permite probar las pantallas "sin conexión" cambiando el valor a mano.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.repository.ConectividadObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConectividadSimulada(conexionInicial: Boolean = true) : ConectividadObserver {

    private val _hayConexion = MutableStateFlow(conexionInicial)
    override val hayConexion: Flow<Boolean> = _hayConexion.asStateFlow()

    fun cambiar(hayConexion: Boolean) {
        _hayConexion.value = hayConexion
    }
}
