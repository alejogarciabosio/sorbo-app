// Capa de presentación · ViewModel de Iniciar sesión (F1, F2): valida, llama a la sesión y expone el estado de la pantalla.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.util.esEmailValido
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EstadoIniciarSesion {
    /** La contraseña vive solo acá, en memoria: nunca se guarda para retomarla si el sistema cierra la app. */
    data class Editando(
        val email: String = "",
        val clave: String = "",
        val enviando: Boolean = false,
        val error: ErrorDeCuenta? = null,
    ) : EstadoIniciarSesion

    data object Listo : EstadoIniciarSesion
}

/**
 * @param alEntrar se llama con el usuario cuando la sesión se abrió (por ejemplo para respaldar lo que ya hay en el
 * teléfono). Si falla, la sesión igual queda abierta.
 */
class IniciarSesionViewModel(
    private val sesion: SesionRepository,
    private val savedStateHandle: SavedStateHandle,
    private val alEntrar: suspend (Usuario) -> Unit = {},
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoIniciarSesion>(
        EstadoIniciarSesion.Editando(email = savedStateHandle.get<String>(CLAVE_EMAIL).orEmpty()),
    )
    val estado: StateFlow<EstadoIniciarSesion> = _estado.asStateFlow()

    fun alCambiarEmail(texto: String) {
        savedStateHandle[CLAVE_EMAIL] = texto
        editar { it.copy(email = texto, error = null) }
    }

    fun alCambiarClave(texto: String) = editar { it.copy(clave = texto, error = null) }

    fun alIniciarSesion() {
        val actual = _estado.value as? EstadoIniciarSesion.Editando ?: return
        if (actual.enviando) return
        val invalido = when {
            !esEmailValido(actual.email) -> ErrorDeCuenta.EmailInvalido
            actual.clave.isEmpty() -> ErrorDeCuenta.ClaveVacia
            else -> null
        }
        if (invalido != null) {
            editar { it.copy(error = invalido) }
            return
        }
        editar { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            sesion.iniciarSesion(actual.email.trim(), actual.clave).fold(
                onSuccess = { usuario ->
                    try {
                        alEntrar(usuario)
                    } catch (cancelacion: CancellationException) {
                        throw cancelacion
                    } catch (falla: Exception) {
                        // La sesión ya está abierta: lo que falló después se reintenta solo más adelante.
                    }
                    _estado.value = EstadoIniciarSesion.Listo
                },
                onFailure = { falla -> editar { it.copy(enviando = false, error = falla.comoErrorDeCuenta()) } },
            )
        }
    }

    private fun editar(cambio: (EstadoIniciarSesion.Editando) -> EstadoIniciarSesion.Editando) =
        _estado.update { (it as? EstadoIniciarSesion.Editando)?.let(cambio) ?: it }

    private companion object {
        const val CLAVE_EMAIL = "email"
    }
}
