// Capa de presentación · ViewModel de Recuperar contraseña (F4, F5): pide el enlace por email y permite reenviarlo.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.util.esEmailValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EstadoRecuperarContrasena {
    data class Editando(
        val email: String = "",
        val enviando: Boolean = false,
        val error: ErrorDeCuenta? = null,
    ) : EstadoRecuperarContrasena

    /** El enlace salió (F5). [reenviado] es verdadero un momento después de pedir otro; [error] si ese pedido falló. */
    data class Enviado(
        val email: String,
        val reenviando: Boolean = false,
        val reenviado: Boolean = false,
        val error: ErrorDeCuenta? = null,
    ) : EstadoRecuperarContrasena
}

class RecuperarContrasenaViewModel(
    private val sesion: SesionRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoRecuperarContrasena>(
        EstadoRecuperarContrasena.Editando(email = savedStateHandle.get<String>(CLAVE_EMAIL).orEmpty()),
    )
    val estado: StateFlow<EstadoRecuperarContrasena> = _estado.asStateFlow()

    fun alCambiarEmail(texto: String) {
        savedStateHandle[CLAVE_EMAIL] = texto
        _estado.update {
            (it as? EstadoRecuperarContrasena.Editando)?.copy(email = texto, error = null) ?: it
        }
    }

    fun alEnviarEnlace() {
        val actual = _estado.value as? EstadoRecuperarContrasena.Editando ?: return
        if (actual.enviando) return
        if (!esEmailValido(actual.email)) {
            _estado.value = actual.copy(error = ErrorDeCuenta.EmailInvalido)
            return
        }
        val correo = actual.email.trim()
        _estado.value = actual.copy(enviando = true, error = null)
        viewModelScope.launch {
            sesion.recuperarContrasena(correo).fold(
                onSuccess = { _estado.value = EstadoRecuperarContrasena.Enviado(correo) },
                onFailure = { falla -> _estado.value = actual.copy(enviando = false, error = falla.comoErrorDeCuenta()) },
            )
        }
    }

    fun alReenviarEnlace() {
        val actual = _estado.value as? EstadoRecuperarContrasena.Enviado ?: return
        if (actual.reenviando) return
        _estado.value = actual.copy(reenviando = true, reenviado = false, error = null)
        viewModelScope.launch {
            sesion.recuperarContrasena(actual.email).fold(
                onSuccess = { _estado.value = actual.copy(reenviando = false, reenviado = true) },
                onFailure = { falla -> _estado.value = actual.copy(reenviando = false, error = falla.comoErrorDeCuenta()) },
            )
        }
    }

    private companion object {
        const val CLAVE_EMAIL = "email"
    }
}
