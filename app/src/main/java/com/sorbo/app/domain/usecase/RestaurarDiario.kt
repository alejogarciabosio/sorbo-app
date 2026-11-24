// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.CambiosPendientes
import com.sorbo.app.domain.model.EstadoRestauracion
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.MotivoRestauracion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.repository.RespaldoRemoto
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Trae el diario de la nube a este teléfono (F8). Baja los vinos por páginas, con sus fotos, y después las
 * degustaciones (la base exige que el vino exista antes). Va publicando el progreso en [estado].
 * Es **reanudable**: si se corta, se vuelve a llamar y no duplica nada (gana el `actualizadoEn` más reciente),
 * y las fotos que ya se bajaron no se vuelven a bajar. Si una foto no se puede bajar, el vino se trae igual sin ella.
 * Hay una sola instancia (en `AppContainer`), así la pantalla ve el mismo progreso aunque se recree.
 */
class RestaurarDiario(
    private val vinos: VinoRepository,
    private val respaldo: RespaldoRemoto,
    private val sesion: SesionRepository,
    private val fotos: AlmacenFotos,
    private val tamanoPagina: Int = TAMANO_PAGINA,
) {

    private val _estado = MutableStateFlow<EstadoRestauracion>(EstadoRestauracion.SinIniciar)
    val estado: StateFlow<EstadoRestauracion> = _estado.asStateFlow()

    suspend operator fun invoke() {
        val usuario = (sesion.sesion.first() as? EstadoSesion.ConSesion)?.usuario
        if (usuario == null) {
            _estado.value = EstadoRestauracion.Error(MotivoRestauracion.SIN_SESION)
            return
        }
        try {
            val total = respaldo.contarVinos(usuario.id)
            if (total == 0) {
                _estado.value = EstadoRestauracion.SinRespaldo
                return
            }
            _estado.value = EstadoRestauracion.Restaurando(bajados = 0, total = total)

            var bajados = 0
            while (true) {
                val pagina = respaldo.bajarVinos(usuario.id, desde = bajados, cantidad = tamanoPagina)
                if (pagina.isEmpty()) break
                vinos.guardarRestaurados(CambiosPendientes(pagina.map { conFotoLocal(it) }, emptyList()))
                bajados += pagina.size
                _estado.value = EstadoRestauracion.Restaurando(bajados = minOf(bajados, total), total = total)
                if (pagina.size < tamanoPagina) break
            }

            var desde = 0
            while (true) {
                val pagina = respaldo.bajarDegustaciones(usuario.id, desde = desde, cantidad = tamanoPagina)
                if (pagina.isEmpty()) break
                vinos.guardarRestaurados(CambiosPendientes(emptyList(), pagina))
                desde += pagina.size
                if (pagina.size < tamanoPagina) break
            }
            _estado.value = EstadoRestauracion.Terminada(vinos = bajados)
        } catch (e: RespaldoException) {
            _estado.value = EstadoRestauracion.Error(motivoDe(e))
        }
    }

    /** Si el vino tiene foto en la nube, la baja (salvo que ya esté en el teléfono de una vuelta anterior). */
    private suspend fun conFotoLocal(vino: Vino): Vino {
        val remota = vino.fotoUrl ?: return vino
        if (vino.eliminado) return vino
        val yaBajada = vinos.listarVinos().firstOrNull { it.id == vino.id && it.fotoUrl == remota }?.fotoPath
        if (yaBajada != null) return vino.copy(fotoPath = yaBajada)
        return try {
            vino.copy(fotoPath = fotos.guardarBytes(respaldo.bajarFoto(remota)).getOrNull())
        } catch (e: RespaldoException) {
            vino
        }
    }

    private fun motivoDe(e: RespaldoException): MotivoRestauracion = when {
        e.cause is CuentaException -> MotivoRestauracion.SIN_SESION
        e.reintentable -> MotivoRestauracion.SIN_CONEXION
        else -> MotivoRestauracion.ERROR_DESCONOCIDO
    }

    companion object {
        const val TAMANO_PAGINA = 100
    }
}
