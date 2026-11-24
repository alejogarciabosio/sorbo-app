// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.CambiosPendientes
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.repository.RespaldoRemoto
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.flow.first

sealed interface ResultadoSincronizacion {
    /** Sin cuenta no se respalda nada. */
    data object SinSesion : ResultadoSincronizacion

    data object NadaPendiente : ResultadoSincronizacion

    data class Sincronizado(val vinos: Int, val degustaciones: Int) : ResultadoSincronizacion

    /** [reintentable]: vale la pena volver a probar más tarde (sin red, servidor caído). Lo pendiente sigue PENDIENTE. */
    data class Fallo(val reintentable: Boolean) : ResultadoSincronizacion
}

/**
 * Sube a la nube lo que está PENDIENTE (incluidas las eliminaciones). Orden: primero las fotos que faltan,
 * después los vinos y por último sus degustaciones (la nube exige que el vino exista antes).
 * Solo se marca SINCRONIZADO lo que no cambió durante la subida (lo resuelve `marcarSincronizado`).
 * Si algo falla no se marca nada: en la próxima vuelta se vuelve a subir, y como es *upsert* no se duplica.
 */
class SincronizarDiario(
    private val vinos: VinoRepository,
    private val respaldo: RespaldoRemoto,
    private val sesion: SesionRepository,
) {

    suspend operator fun invoke(): ResultadoSincronizacion {
        val usuario = (sesion.sesion.first() as? EstadoSesion.ConSesion)?.usuario
            ?: return ResultadoSincronizacion.SinSesion

        // Lo que se cargó con la sesión abierta todavía no tiene dueño: es de quien está conectado.
        vinos.asignarUsuario(usuario.id)
        val pendientes = vinos.listarPendientes()

        // Solo lo de este usuario: la seguridad de la nube rechazaría vinos de otra cuenta del mismo teléfono.
        val vinosDelUsuario = pendientes.vinos.filter { it.usuarioId == usuario.id }
        val idsDelUsuario = vinosDelUsuario.map { it.id }.toSet() +
            vinos.listarVinos().filter { it.usuarioId == usuario.id }.map { it.id }
        val degustaciones = pendientes.degustaciones.filter { it.vinoId in idsDelUsuario }
        if (vinosDelUsuario.isEmpty() && degustaciones.isEmpty()) return ResultadoSincronizacion.NadaPendiente

        return try {
            val conFoto = vinosDelUsuario.map { vino ->
                val ruta = vino.fotoPath
                if (ruta != null && vino.fotoUrl == null && !vino.eliminado) {
                    vino.copy(fotoUrl = respaldo.subirFoto(usuario.id, vino.id, ruta))
                } else {
                    vino
                }
            }
            if (conFoto.isNotEmpty()) respaldo.guardarVinos(conFoto)
            if (degustaciones.isNotEmpty()) respaldo.guardarDegustaciones(degustaciones)
            vinos.marcarSincronizado(CambiosPendientes(conFoto, degustaciones))
            ResultadoSincronizacion.Sincronizado(conFoto.size, degustaciones.size)
        } catch (e: RespaldoException) {
            ResultadoSincronizacion.Fallo(e.reintentable)
        }
    }
}
