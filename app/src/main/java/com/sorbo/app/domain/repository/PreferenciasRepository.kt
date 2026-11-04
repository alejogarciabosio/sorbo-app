// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Preferencias simples que viven en el teléfono (la implementación real usa DataStore). */
interface PreferenciasRepository {

    val bienvenidaVista: Flow<Boolean>

    /** Si ya aceptó el aviso de la primera vez que se usa la IA (RNF06). */
    val avisoIaAceptado: Flow<Boolean>

    /** Por defecto activada. Desactivarla impide enviar nada a la nube desde Ajustes. */
    val iaActivada: Flow<Boolean>

    /** Las últimas búsquedas, la más nueva primero; ver [com.sorbo.app.domain.util.agregarBusquedaReciente]. */
    val busquedasRecientes: Flow<List<String>>

    suspend fun marcarBienvenidaVista()

    suspend fun aceptarAvisoIa()

    suspend fun cambiarIaActivada(activada: Boolean)

    suspend fun agregarBusquedaReciente(texto: String)

    suspend fun borrarBusquedasRecientes()
}
