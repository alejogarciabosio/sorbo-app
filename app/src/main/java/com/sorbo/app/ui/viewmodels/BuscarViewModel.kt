// Capa de presentación · ViewModel: guarda el estado de la pantalla (StateFlow inmutable) y habla con el Repository.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.usecase.BuscarEnDiario
import com.sorbo.app.domain.usecase.ResultadoBusqueda
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

/** Lo que muestra la pantalla de búsqueda debajo del campo de texto. */
sealed interface EstadoBusqueda {
    /** Todavía no escribió nada. */
    data object SinConsulta : EstadoBusqueda
    data class ConResultado(val resultado: ResultadoBusqueda) : EstadoBusqueda
}

@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModel(buscarEnDiario: BuscarEnDiario) : ViewModel() {

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    /** Se recalcula con cada letra; si llega otra consulta, la anterior se descarta. */
    val estado: StateFlow<EstadoBusqueda> = _consulta
        .mapLatest { texto ->
            if (texto.isBlank()) EstadoBusqueda.SinConsulta else EstadoBusqueda.ConResultado(buscarEnDiario(texto))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoBusqueda.SinConsulta)

    fun alCambiarConsulta(texto: String) {
        _consulta.value = texto
    }
}
