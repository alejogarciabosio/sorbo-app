// Capa de datos · disparador: pide sincronizar cada vez que hay cambios pendientes y una sesión abierta.
package com.sorbo.app.data.sync

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.ProgramadorDeSincronizacion
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * En lugar de tocar cada caso de uso (guardar, editar, eliminar, deshacer…), escucha la cantidad de pendientes:
 * cualquier escritura la cambia, y si hay algo pendiente y una cuenta abierta, se programa el respaldo.
 * Al arrancar la app emite el valor actual, así que también cubre "sincronizar al iniciar".
 */
class DisparadorDeSincronizacion(
    private val vinos: VinoRepository,
    private val sesion: SesionRepository,
    private val programador: ProgramadorDeSincronizacion,
) {

    fun iniciar(alcance: CoroutineScope): Job =
        combine(vinos.observarCantidadPendientes(), sesion.sesion) { pendientes, estado ->
            pendientes > 0 && estado is EstadoSesion.ConSesion
        }
            .filter { hayQueSubir -> hayQueSubir }
            .onEach { programador.programar() }
            .launchIn(alcance)
}
