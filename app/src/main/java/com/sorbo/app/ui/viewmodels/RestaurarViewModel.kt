// Capa de presentación · ViewModel de Restaurar diario (F8): arranca la restauración y expone su progreso.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.EstadoRestauracion
import com.sorbo.app.domain.usecase.RestaurarDiario
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * El caso de uso es una sola instancia que recuerda su último estado; esta pantalla siempre arranca en [EstadoRestauracion.SinIniciar]
 * (la oferta) hasta que la persona toca "Restaurar", y desde ahí muestra el progreso real.
 */
class RestaurarViewModel(private val restaurar: RestaurarDiario) : ViewModel() {

    private val iniciada = MutableStateFlow(false)
    private var trabajo: Job? = null

    val estado: StateFlow<EstadoRestauracion> = combine(iniciada, restaurar.estado) { arranco, actual ->
        if (arranco) actual else EstadoRestauracion.SinIniciar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoRestauracion.SinIniciar)

    /** "Restaurar mi diario" y también "Reintentar": retoma sin duplicar. */
    fun alRestaurar() {
        if (trabajo?.isActive == true) return
        iniciada.value = true
        trabajo = viewModelScope.launch { restaurar() }
    }
}
