// Capa de datos · preferencias simuladas en memoria: sirven para las pantallas y los tests hasta que esté DataStore.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.util.agregarBusquedaReciente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PreferenciasRepositoryEnMemoria : PreferenciasRepository {

    private val _bienvenidaVista = MutableStateFlow(false)
    private val _avisoIaAceptado = MutableStateFlow(false)
    private val _iaActivada = MutableStateFlow(true)
    private val _busquedasRecientes = MutableStateFlow<List<String>>(emptyList())

    override val bienvenidaVista: Flow<Boolean> = _bienvenidaVista.asStateFlow()
    override val avisoIaAceptado: Flow<Boolean> = _avisoIaAceptado.asStateFlow()
    override val iaActivada: Flow<Boolean> = _iaActivada.asStateFlow()
    override val busquedasRecientes: Flow<List<String>> = _busquedasRecientes.asStateFlow()

    override suspend fun marcarBienvenidaVista() {
        _bienvenidaVista.value = true
    }

    override suspend fun aceptarAvisoIa() {
        _avisoIaAceptado.value = true
    }

    override suspend fun cambiarIaActivada(activada: Boolean) {
        _iaActivada.value = activada
    }

    override suspend fun agregarBusquedaReciente(texto: String) {
        _busquedasRecientes.update { agregarBusquedaReciente(it, texto) }
    }

    override suspend fun borrarBusquedasRecientes() {
        _busquedasRecientes.value = emptyList()
    }
}
