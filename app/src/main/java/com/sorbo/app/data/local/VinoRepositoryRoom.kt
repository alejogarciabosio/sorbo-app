// Capa de datos · repositorio con Room: implementación real de VinoRepository, guarda el diario en el teléfono.
package com.sorbo.app.data.local

import com.sorbo.app.data.mapper.aDominio
import com.sorbo.app.data.mapper.aEntidad
import com.sorbo.app.domain.model.CambiosPendientes
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.util.coincideConConsulta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

/**
 * Room es la fuente de verdad: los datos sobreviven a cerrar la app. Cumple el mismo contrato que
 * `VinoRepositoryEnMemoria` (lo comprueban los tests de contrato) y nunca entrega entidades: siempre modelos del dominio.
 * Toda escritura deja el registro como PENDIENTE de respaldo, con `actualizadoEn` = ahora.
 */
class VinoRepositoryRoom(
    private val dao: VinoDao,
    private val reloj: Clock = Clock.systemUTC(),
) : VinoRepository {

    override fun observarDiario(): Flow<List<VinoConDegustaciones>> =
        dao.observarDiario().map { filas -> ordenarComoDiario(filas.map { it.aDominio() }) }

    override fun observarVino(id: String): Flow<VinoConDegustaciones?> =
        dao.observarVino(id).map { it?.aDominio() }

    override suspend fun listarVinos(): List<Vino> = dao.listarVinos().map { it.aDominio() }

    // Se filtra en Kotlin con la misma regla que usa la versión en memoria (todas las palabras de la consulta).
    override suspend fun buscar(consultaNormalizada: String): List<VinoConDegustaciones> =
        ordenarComoDiario(dao.listarDiario().map { it.aDominio() })
            .filter { coincideConConsulta(it.vino.textoBusqueda, consultaNormalizada) }

    override suspend fun guardarVinoNuevo(vino: Vino, degustacion: Degustacion) {
        dao.insertarVinoConDegustacion(vino.aEntidad(), degustacion.aEntidad())
    }

    override suspend fun agregarDegustacion(degustacion: Degustacion) {
        require(dao.existeVino(degustacion.vinoId)) { "No existe el vino ${degustacion.vinoId}" }
        dao.insertarDegustacion(degustacion.aEntidad())
    }

    override suspend fun actualizarVino(vino: Vino) {
        dao.actualizarVino(vino.copy(actualizadoEn = reloj.instant(), estadoSync = EstadoSync.PENDIENTE).aEntidad())
    }

    override suspend fun eliminarDegustacion(id: String) {
        dao.eliminarDegustacion(id, reloj.instant(), EstadoSync.PENDIENTE)
    }

    override suspend fun eliminarVino(id: String) = dao.cambiarEliminado(id, eliminado = true, ahora = reloj.instant())

    override suspend fun restaurarVino(id: String) = dao.cambiarEliminado(id, eliminado = false, ahora = reloj.instant())

    override suspend fun listarPendientes(): CambiosPendientes = CambiosPendientes(
        vinos = dao.listarVinosEn(EstadoSync.PENDIENTE).map { it.aDominio() },
        degustaciones = dao.listarDegustacionesEn(EstadoSync.PENDIENTE).map { it.aDominio() },
    )

    override suspend fun marcarSincronizado(subidos: CambiosPendientes) {
        dao.marcarSincronizados(subidos.vinos.map { it.aEntidad() }, subidos.degustaciones.map { it.aEntidad() })
    }

    override fun observarCantidadPendientes(): Flow<Int> = dao.observarCantidadEn(EstadoSync.PENDIENTE)

    /** El probado más recientemente primero; si empatan (o no tienen degustaciones), el creado más recientemente. */
    private fun ordenarComoDiario(lista: List<VinoConDegustaciones>): List<VinoConDegustaciones> =
        lista.sortedWith(
            compareByDescending<VinoConDegustaciones> { it.ultimaFecha ?: LocalDate.MIN }
                .thenByDescending { it.vino.creadoEn },
        )
}
