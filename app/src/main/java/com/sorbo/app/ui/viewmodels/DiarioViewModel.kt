// Capa de presentación · ViewModel: guarda el estado de la pantalla (StateFlow inmutable) y habla con el Repository.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.ConectividadObserver
import com.sorbo.app.domain.repository.SesionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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

    /** A3/A4: no se pudo leer el diario del teléfono. No se perdió nada; se puede reintentar. */
    data object ConError : EstadoDiario
}

/**
 * @param sesion con cuenta se muestra qué quedó sin respaldar; sin cuenta nunca se va a respaldar nada, así que no se avisa.
 * @param conectividad para el aviso de "sin conexión": el diario funciona igual, solo se avisa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiarioViewModel(
    repositorio: VinoRepository,
    sesion: SesionRepository? = null,
    conectividad: ConectividadObserver? = null,
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)

    /** El estado del diario; el repositorio ya entrega los vinos del probado más recientemente al más antiguo. */
    val estado: StateFlow<EstadoDiario> = reintentos
        .flatMapLatest {
            repositorio.observarDiario()
                .map<List<VinoConDegustaciones>, EstadoDiario> { vinos ->
                    if (vinos.isEmpty()) EstadoDiario.Vacio else EstadoDiario.ConVinos(vinos)
                }
                .catch { emit(EstadoDiario.ConError) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoDiario.Cargando)

    /** Verdadero cuando el teléfono no tiene internet. */
    val sinConexion: StateFlow<Boolean> = (conectividad?.hayConexion ?: flowOf(true))
        .map { hayConexion -> !hayConexion }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Solo quien tiene cuenta ve "Pendiente" en sus vinos. */
    val conCuenta: StateFlow<Boolean> = (sesion?.sesion ?: flowOf(EstadoSesion.SinSesion))
        .map { actual -> actual is EstadoSesion.ConSesion }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Cambios que todavía no se respaldaron (0 sin cuenta). */
    val pendientes: StateFlow<Int> = conCuenta
        .flatMapLatest { conSesion -> if (conSesion) repositorio.observarCantidadPendientes() else flowOf(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun alReintentar() {
        reintentos.update { it + 1 }
    }
}
