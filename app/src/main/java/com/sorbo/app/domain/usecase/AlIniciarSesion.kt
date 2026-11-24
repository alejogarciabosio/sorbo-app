// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.ProgramadorDeSincronizacion
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.repository.RespaldoRemoto
import com.sorbo.app.domain.repository.VinoRepository

/** Lo que la pantalla necesita saber después de iniciar sesión o crear cuenta (F7 y F8). */
data class ResultadoInicioSesion(
    /** Vinos del teléfono que se van a respaldar en la nube (F7: "Respaldamos tus N vinos"). */
    val vinosParaRespaldar: Int,
    /** Hay vinos en la nube que se pueden traer a este teléfono (F8). Si no hay red para consultarlo, es false. */
    val hayRespaldoParaRestaurar: Boolean,
)

/**
 * Lo que pasa justo después de iniciar sesión o crear cuenta: los vinos cargados sin cuenta pasan a ser
 * de este usuario, se pide el respaldo en segundo plano y se averigua si hay algo en la nube para restaurar.
 * Lo llaman las pantallas de cuenta; nunca bloquea: si la nube no responde, igual se puede seguir usando la app.
 */
class AlIniciarSesion(
    private val vinos: VinoRepository,
    private val respaldo: RespaldoRemoto,
    private val programador: ProgramadorDeSincronizacion,
) {

    suspend operator fun invoke(usuario: Usuario): ResultadoInicioSesion {
        vinos.asignarUsuario(usuario.id)
        val paraRespaldar = vinos.listarPendientes().vinos.count { it.usuarioId == usuario.id && !it.eliminado }
        programador.programar()
        val hayRespaldo = try {
            respaldo.hayRespaldo(usuario.id)
        } catch (e: RespaldoException) {
            false
        }
        return ResultadoInicioSesion(vinosParaRespaldar = paraRespaldar, hayRespaldoParaRestaurar = hayRespaldo)
    }
}
