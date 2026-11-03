// Capa de presentación · ViewModel: guarda el estado de la pantalla (StateFlow inmutable) y habla con el Repository.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Lo que puede mostrar la pantalla del diario. */
sealed interface EstadoDiario {
    data object Cargando : EstadoDiario
    data object Vacio : EstadoDiario
    data class ConVinos(val vinos: List<VinoConDegustaciones>) : EstadoDiario
}

class DiarioViewModel(repositorio: VinoRepository) : ViewModel() {

    /** El estado del diario; el repositorio ya entrega los vinos del probado más recientemente al más antiguo. */
    val estado: StateFlow<EstadoDiario> = repositorio.observarDiario()
        .map { vinos ->
            if (vinos.isEmpty()) {
                EstadoDiario.Vacio
            } else {
                EstadoDiario.ConVinos(vinos)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoDiario.Cargando)
}
