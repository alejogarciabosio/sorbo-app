// Capa de presentación · ViewModel de Nueva contraseña (F6) y Cambiar contraseña (G4): elegir y confirmar la contraseña nueva.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.util.esClaveValida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EstadoNuevaContrasena {
    /** Las contraseñas viven solo en memoria: nunca se guardan para retomarlas si el sistema cierra la app. */
    data class Editando(
        val clave: String = "",
        val repetirClave: String = "",
        val enviando: Boolean = false,
        val error: ErrorDeCuenta? = null,
    ) : EstadoNuevaContrasena {
        val claveCumpleElMinimo: Boolean get() = esClaveValida(clave)
    }

    data object Listo : EstadoNuevaContrasena
}

/** Sirve para quien llegó desde el enlace del email y para quien la cambia desde Ajustes: los dos tienen una sesión abierta. */
class NuevaContrasenaViewModel(private val sesion: SesionRepository) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoNuevaContrasena>(EstadoNuevaContrasena.Editando())
    val estado: StateFlow<EstadoNuevaContrasena> = _estado.asStateFlow()

    fun alCambiarClave(texto: String) = editar { it.copy(clave = texto, error = null) }
    fun alCambiarRepetirClave(texto: String) = editar { it.copy(repetirClave = texto, error = null) }

    fun alGuardar() {
        val actual = _estado.value as? EstadoNuevaContrasena.Editando ?: return
        if (actual.enviando) return
        val invalido = when {
            !esClaveValida(actual.clave) -> ErrorDeCuenta.ClaveCorta
            actual.clave != actual.repetirClave -> ErrorDeCuenta.ClavesDistintas
            else -> null
        }
        if (invalido != null) {
            editar { it.copy(error = invalido) }
            return
        }
        editar { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            sesion.cambiarContrasena(actual.clave).fold(
                onSuccess = { _estado.value = EstadoNuevaContrasena.Listo },
                onFailure = { falla -> editar { it.copy(enviando = false, error = falla.comoErrorDeCuenta()) } },
            )
        }
    }

    private fun editar(cambio: (EstadoNuevaContrasena.Editando) -> EstadoNuevaContrasena.Editando) =
        _estado.update { (it as? EstadoNuevaContrasena.Editando)?.let(cambio) ?: it }
}
