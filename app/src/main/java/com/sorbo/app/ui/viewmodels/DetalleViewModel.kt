// Capa de presentación · ViewModel: guarda el estado del detalle de un vino (StateFlow inmutable) y habla con los casos de uso.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.usecase.DetalleVino
import com.sorbo.app.domain.usecase.EliminarVino
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que muestra la pantalla de detalle. */
sealed interface EstadoDetalle {
    data object Cargando : EstadoDetalle
    data class Contenido(val detalle: DetalleVino) : EstadoDetalle

    /** El vino no existe (o ya se eliminó): no hay nada que mostrar. */
    data object NoEncontrado : EstadoDetalle
}

/** Se eliminó el vino: la pantalla vuelve atrás y ofrece "Deshacer" con estos datos. */
data class VinoEliminado(val vinoId: String, val nombre: String)

class DetalleViewModel(
    private val vinoId: String,
    obtenerDetalleVino: ObtenerDetalleVino,
    private val eliminarVino: EliminarVino,
) : ViewModel() {

    val estado: StateFlow<EstadoDetalle> = obtenerDetalleVino(vinoId)
        .map { detalle -> if (detalle == null) EstadoDetalle.NoEncontrado else EstadoDetalle.Contenido(detalle) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoDetalle.Cargando)

    private val _eliminado = MutableStateFlow<VinoEliminado?>(null)
    val eliminado: StateFlow<VinoEliminado?> = _eliminado.asStateFlow()

    private var eliminando = false

    fun alEliminar() {
        val contenido = estado.value as? EstadoDetalle.Contenido ?: return
        if (eliminando) return
        eliminando = true
        viewModelScope.launch {
            eliminarVino(vinoId)
            _eliminado.value = VinoEliminado(vinoId, contenido.detalle.vino.nombre)
        }
    }
}
