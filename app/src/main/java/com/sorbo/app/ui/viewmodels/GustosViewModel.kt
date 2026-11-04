// Capa de presentación · ViewModel: calcula las estadísticas de gustos en el teléfono a partir del diario y las expone como estado.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.usecase.CalcularEstadisticasGustos
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Lo que puede mostrar "Mis gustos". */
sealed interface EstadoGustos {
    data object Cargando : EstadoGustos

    /** El diario no tiene ninguna degustación. */
    data object Vacio : EstadoGustos

    /** Hay algunas degustaciones, pero menos de las necesarias: [faltan] para habilitar el análisis. */
    data class PocasDegustaciones(val registradas: Int, val faltan: Int, val necesarias: Int) : EstadoGustos

    data class ConEstadisticas(val estadisticas: EstadisticasGustos) : EstadoGustos
}

class GustosViewModel(
    repositorio: VinoRepository,
    calcularEstadisticasGustos: CalcularEstadisticasGustos,
) : ViewModel() {

    /** Se recalcula cada vez que el diario cambia, así que siempre está al día y no necesita conexión. */
    val estado: StateFlow<EstadoGustos> = repositorio.observarDiario()
        .map { vinos -> aEstado(vinos, calcularEstadisticasGustos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoGustos.Cargando)

    private fun aEstado(vinos: List<VinoConDegustaciones>, calcular: CalcularEstadisticasGustos): EstadoGustos {
        val estadisticas = calcular(vinos.flatMap { it.degustaciones })
        return when {
            estadisticas.totalDegustaciones == 0 -> EstadoGustos.Vacio
            !estadisticas.habilitado -> EstadoGustos.PocasDegustaciones(
                registradas = estadisticas.totalDegustaciones,
                faltan = estadisticas.faltan,
                necesarias = CalcularEstadisticasGustos.MINIMO_DEGUSTACIONES,
            )
            else -> EstadoGustos.ConEstadisticas(estadisticas)
        }
    }
}
