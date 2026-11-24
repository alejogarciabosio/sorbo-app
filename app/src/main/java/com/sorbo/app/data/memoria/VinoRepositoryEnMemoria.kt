// Capa de datos · repositorio en memoria: implementación provisoria hasta que esté Room.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.model.CambiosPendientes
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.util.coincideConConsulta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate

/**
 * Repositorio que guarda todo en memoria. Sirve para desarrollar las pantallas y para los tests
 * mientras no existe Room; respeta el mismo contrato que [VinoRepository].
 */
class VinoRepositoryEnMemoria(
    private val reloj: Clock = Clock.systemUTC(),
    iniciales: List<Pair<Vino, List<Degustacion>>> = emptyList(),
) : VinoRepository {

    private data class Datos(val vinos: List<Vino>, val degustaciones: List<Degustacion>)

    private val estado = MutableStateFlow(
        Datos(vinos = iniciales.map { it.first }, degustaciones = iniciales.flatMap { it.second }),
    )

    private fun armar(datos: Datos): List<VinoConDegustaciones> =
        datos.vinos.filter { !it.eliminado }
            .map { vino ->
                VinoConDegustaciones(vino, datos.degustaciones.filter { it.vinoId == vino.id && !it.eliminado })
            }
            .sortedWith(
                compareByDescending<VinoConDegustaciones> { it.ultimaFecha ?: LocalDate.MIN }
                    .thenByDescending { it.vino.creadoEn },
            )

    override fun observarDiario(): Flow<List<VinoConDegustaciones>> = estado.map(::armar)

    override fun observarVino(id: String): Flow<VinoConDegustaciones?> =
        estado.map { datos -> armar(datos).firstOrNull { it.vino.id == id } }

    override suspend fun listarVinos(): List<Vino> = estado.value.vinos.filter { !it.eliminado }

    override suspend fun buscar(consultaNormalizada: String): List<VinoConDegustaciones> =
        armar(estado.value).filter { coincideConConsulta(it.vino.textoBusqueda, consultaNormalizada) }

    override suspend fun guardarVinoNuevo(vino: Vino, degustacion: Degustacion) {
        estado.update { it.copy(vinos = it.vinos + vino, degustaciones = it.degustaciones + degustacion) }
    }

    override suspend fun agregarDegustacion(degustacion: Degustacion) {
        estado.update { datos ->
            require(datos.vinos.any { it.id == degustacion.vinoId && !it.eliminado }) {
                "No existe el vino ${degustacion.vinoId}"
            }
            datos.copy(degustaciones = datos.degustaciones + degustacion)
        }
    }

    override suspend fun actualizarVino(vino: Vino) {
        val ahora = reloj.instant()
        estado.update { datos ->
            datos.copy(
                vinos = datos.vinos.map {
                    if (it.id == vino.id) vino.copy(actualizadoEn = ahora, estadoSync = EstadoSync.PENDIENTE) else it
                },
            )
        }
    }

    override suspend fun eliminarDegustacion(id: String) {
        val ahora = reloj.instant()
        estado.update { datos ->
            datos.copy(
                degustaciones = datos.degustaciones.map {
                    if (it.id == id) it.copy(eliminado = true, actualizadoEn = ahora, estadoSync = EstadoSync.PENDIENTE) else it
                },
            )
        }
    }

    override suspend fun eliminarVino(id: String) = cambiarEliminado(id, eliminado = true)

    override suspend fun restaurarVino(id: String) = cambiarEliminado(id, eliminado = false)

    private fun cambiarEliminado(id: String, eliminado: Boolean) {
        val ahora = reloj.instant()
        estado.update { datos ->
            datos.copy(
                vinos = datos.vinos.map {
                    if (it.id == id) it.copy(eliminado = eliminado, actualizadoEn = ahora, estadoSync = EstadoSync.PENDIENTE) else it
                },
                degustaciones = datos.degustaciones.map {
                    if (it.vinoId == id) it.copy(eliminado = eliminado, actualizadoEn = ahora, estadoSync = EstadoSync.PENDIENTE) else it
                },
            )
        }
    }

    override suspend fun listarPendientes(): CambiosPendientes {
        val datos = estado.value
        return CambiosPendientes(
            vinos = datos.vinos.filter { it.estadoSync == EstadoSync.PENDIENTE },
            degustaciones = datos.degustaciones.filter { it.estadoSync == EstadoSync.PENDIENTE },
        )
    }

    override suspend fun marcarSincronizado(subidos: CambiosPendientes) {
        val vinosSubidos = subidos.vinos.associateBy { it.id }
        val degustacionesSubidas = subidos.degustaciones.associateBy { it.id }
        estado.update { datos ->
            datos.copy(
                vinos = datos.vinos.map { actual ->
                    val subido = vinosSubidos[actual.id]
                    if (subido != null && subido.actualizadoEn == actual.actualizadoEn) {
                        actual.copy(estadoSync = EstadoSync.SINCRONIZADO, fotoUrl = subido.fotoUrl)
                    } else {
                        actual
                    }
                },
                degustaciones = datos.degustaciones.map { actual ->
                    val subida = degustacionesSubidas[actual.id]
                    if (subida != null && subida.actualizadoEn == actual.actualizadoEn) {
                        actual.copy(estadoSync = EstadoSync.SINCRONIZADO)
                    } else {
                        actual
                    }
                },
            )
        }
    }

    override suspend fun asignarUsuario(usuarioId: String) {
        val ahora = reloj.instant()
        estado.update { datos ->
            datos.copy(
                vinos = datos.vinos.map {
                    if (it.usuarioId == null) {
                        it.copy(usuarioId = usuarioId, actualizadoEn = ahora, estadoSync = EstadoSync.PENDIENTE)
                    } else {
                        it
                    }
                },
            )
        }
    }

    override fun observarCantidadPendientes(): Flow<Int> = estado.map { datos ->
        datos.vinos.count { it.estadoSync == EstadoSync.PENDIENTE } +
            datos.degustaciones.count { it.estadoSync == EstadoSync.PENDIENTE }
    }
}
