// Capa de presentación · ViewModel de Crear cuenta (F3, F7): valida cada dato antes de llamar a la red y avisa cuántos vinos se respaldan.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.util.esClaveValida
import com.sorbo.app.domain.util.esEmailValido
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EstadoCrearCuenta {
    /** Las contraseñas viven solo en memoria: nunca se guardan para retomarlas si el sistema cierra la app. */
    data class Editando(
        val email: String = "",
        val clave: String = "",
        val repetirClave: String = "",
        val aceptoTerminos: Boolean = false,
        val enviando: Boolean = false,
        val error: ErrorDeCuenta? = null,
    ) : EstadoCrearCuenta {
        /** El requisito de la contraseña se tilda mientras se escribe. */
        val claveCumpleElMinimo: Boolean get() = esClaveValida(clave)
    }

    /** La cuenta se creó. [vinosDelTelefono] son los que se van a respaldar (F7). */
    data class Listo(val vinosDelTelefono: Int) : EstadoCrearCuenta
}

/**
 * @param alEntrar se llama con el usuario nuevo para respaldar lo que ya hay en el teléfono; si falla, la cuenta igual existe.
 * @param contarVinosDelTelefono cuántos vinos hay hoy en el diario, para decírselo a la persona.
 */
class CrearCuentaViewModel(
    private val sesion: SesionRepository,
    private val savedStateHandle: SavedStateHandle,
    private val contarVinosDelTelefono: suspend () -> Int = { 0 },
    private val alEntrar: suspend (Usuario) -> Unit = {},
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoCrearCuenta>(
        EstadoCrearCuenta.Editando(email = savedStateHandle.get<String>(CLAVE_EMAIL).orEmpty()),
    )
    val estado: StateFlow<EstadoCrearCuenta> = _estado.asStateFlow()

    fun alCambiarEmail(texto: String) {
        savedStateHandle[CLAVE_EMAIL] = texto
        editar { it.copy(email = texto, error = null) }
    }

    fun alCambiarClave(texto: String) = editar { it.copy(clave = texto, error = null) }
    fun alCambiarRepetirClave(texto: String) = editar { it.copy(repetirClave = texto, error = null) }
    fun alCambiarTerminos(acepto: Boolean) = editar { it.copy(aceptoTerminos = acepto, error = null) }

    fun alCrearCuenta() {
        val actual = _estado.value as? EstadoCrearCuenta.Editando ?: return
        if (actual.enviando) return
        val invalido = when {
            !esEmailValido(actual.email) -> ErrorDeCuenta.EmailInvalido
            !esClaveValida(actual.clave) -> ErrorDeCuenta.ClaveCorta
            actual.clave != actual.repetirClave -> ErrorDeCuenta.ClavesDistintas
            !actual.aceptoTerminos -> ErrorDeCuenta.TerminosSinAceptar
            else -> null
        }
        if (invalido != null) {
            editar { it.copy(error = invalido) }
            return
        }
        editar { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            sesion.crearCuenta(actual.email.trim(), actual.clave).fold(
                onSuccess = { usuario ->
                    val vinos = contarSinFallar()
                    try {
                        alEntrar(usuario)
                    } catch (cancelacion: CancellationException) {
                        throw cancelacion
                    } catch (falla: Exception) {
                        // La cuenta ya existe: el respaldo se reintenta solo más adelante.
                    }
                    _estado.value = EstadoCrearCuenta.Listo(vinos)
                },
                onFailure = { falla -> editar { it.copy(enviando = false, error = falla.comoErrorDeCuenta()) } },
            )
        }
    }

    private suspend fun contarSinFallar(): Int = try {
        contarVinosDelTelefono()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (falla: Exception) {
        0
    }

    private fun editar(cambio: (EstadoCrearCuenta.Editando) -> EstadoCrearCuenta.Editando) =
        _estado.update { (it as? EstadoCrearCuenta.Editando)?.let(cambio) ?: it }

    private companion object {
        const val CLAVE_EMAIL = "email"
    }
}
