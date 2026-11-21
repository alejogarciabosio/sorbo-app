// Capa de presentación · ViewModel de Ajustes y Mi cuenta (G1 a G6): sesión, respaldo, control de la IA y las acciones con confirmación.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que muestra Ajustes: con cuenta (G1), sin cuenta (G2) o mientras se lee la sesión guardada. */
sealed interface EstadoAjustes {
    data object Cargando : EstadoAjustes

    /** Sin sesión la IA no se puede usar, así que el interruptor queda apagado y deshabilitado. */
    data object SinSesion : EstadoAjustes

    /** [pendientes] son los cambios (vinos y degustaciones) que todavía no se respaldaron: 0 es "Respaldo al día". */
    data class ConSesion(val email: String, val pendientes: Int, val iaActivada: Boolean) : EstadoAjustes
}

/** El cuadro de confirmación o de progreso que está a la vista, si hay alguno. */
sealed interface DialogoAjustes {
    data object Ninguno : DialogoAjustes

    /** G5: cerrar sesión con vinos sin respaldar. */
    data class ConfirmarCerrarSesion(val pendientes: Int) : DialogoAjustes

    /** G6: eliminar la cuenta, con su consecuencia a la vista. */
    data object ConfirmarEliminarCuenta : DialogoAjustes

    data object Eliminando : DialogoAjustes

    data class FalloAlEliminar(val motivo: MotivoCuenta) : DialogoAjustes
}

class AjustesViewModel(
    private val sesion: SesionRepository,
    private val preferencias: PreferenciasRepository,
    repositorio: VinoRepository,
) : ViewModel() {

    private val _dialogo = MutableStateFlow<DialogoAjustes>(DialogoAjustes.Ninguno)
    val dialogo: StateFlow<DialogoAjustes> = _dialogo

    val estado: StateFlow<EstadoAjustes> =
        combine(sesion.sesion, preferencias.iaActivada, repositorio.observarCantidadPendientes()) { actual, ia, pendientes ->
            when (actual) {
                EstadoSesion.SinSesion -> EstadoAjustes.SinSesion
                is EstadoSesion.ConSesion -> EstadoAjustes.ConSesion(actual.usuario.email, pendientes, ia)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoAjustes.Cargando)

    fun alCambiarIa(activada: Boolean) {
        viewModelScope.launch { preferencias.cambiarIaActivada(activada) }
    }

    /** Si hay vinos sin respaldar se avisa antes (G5); si no, se cierra la sesión directo. */
    fun alPedirCerrarSesion() {
        val actual = estado.value as? EstadoAjustes.ConSesion ?: return
        if (actual.pendientes > 0) {
            _dialogo.value = DialogoAjustes.ConfirmarCerrarSesion(actual.pendientes)
        } else {
            viewModelScope.launch { sesion.cerrarSesion() }
        }
    }

    fun alConfirmarCerrarSesion() {
        _dialogo.value = DialogoAjustes.Ninguno
        viewModelScope.launch { sesion.cerrarSesion() }
    }

    fun alPedirEliminarCuenta() {
        if (estado.value is EstadoAjustes.ConSesion) _dialogo.value = DialogoAjustes.ConfirmarEliminarCuenta
    }

    /** Borra la cuenta y su respaldo. Los vinos del teléfono no se tocan. */
    fun alConfirmarEliminarCuenta() {
        if (_dialogo.value == DialogoAjustes.Eliminando) return
        _dialogo.value = DialogoAjustes.Eliminando
        viewModelScope.launch {
            sesion.eliminarCuenta().fold(
                onSuccess = { _dialogo.value = DialogoAjustes.Ninguno },
                onFailure = { falla ->
                    val motivo = (falla as? com.sorbo.app.domain.repository.CuentaException)?.motivo ?: MotivoCuenta.ERROR_DESCONOCIDO
                    _dialogo.value = DialogoAjustes.FalloAlEliminar(motivo)
                },
            )
        }
    }

    fun alCerrarDialogo() {
        if (_dialogo.value != DialogoAjustes.Eliminando) _dialogo.value = DialogoAjustes.Ninguno
    }
}
