// Capa de presentación · ViewModel de la pantalla de arranque: decide si se muestra la bienvenida (O2) sin parpadeos.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Qué se muestra al abrir la app. La preferencia se lee de forma asíncrona: mientras tanto no se muestra ni una cosa ni la otra. */
sealed interface EstadoInicio {
    data object Cargando : EstadoInicio
    data object Bienvenida : EstadoInicio
    data object Listo : EstadoInicio
}

class InicioViewModel(private val preferencias: PreferenciasRepository) : ViewModel() {

    val estado: StateFlow<EstadoInicio> = preferencias.bienvenidaVista
        .map { vista -> if (vista) EstadoInicio.Listo else EstadoInicio.Bienvenida }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoInicio.Cargando)

    private val _irAIniciarSesion = MutableStateFlow(false)

    /** Verdadero cuando la persona tocó "Ya tengo cuenta" en la bienvenida: la navegación abre Iniciar sesión y lo consume. */
    val irAIniciarSesion: StateFlow<Boolean> = _irAIniciarSesion

    /** "Empezar": la bienvenida no vuelve a aparecer. */
    fun alEmpezar() {
        viewModelScope.launch { preferencias.marcarBienvenidaVista() }
    }

    /** "Ya tengo cuenta": también cuenta como bienvenida vista, y se va directo a iniciar sesión. */
    fun alTenerCuenta() {
        _irAIniciarSesion.value = true
        alEmpezar()
    }

    fun alConsumirIrAIniciarSesion() {
        _irAIniciarSesion.value = false
    }
}
