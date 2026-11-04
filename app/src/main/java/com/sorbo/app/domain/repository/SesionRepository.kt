// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/** Por qué falló una operación de cuenta. La pantalla lo traduce a un mensaje en lenguaje simple. */
enum class MotivoCuenta {
    CREDENCIALES_INCORRECTAS,
    EMAIL_YA_REGISTRADO,
    EMAIL_INVALIDO,
    CLAVE_DEBIL,
    SIN_CONEXION,
    SIN_SESION,
    ERROR_DESCONOCIDO,
}

class CuentaException(val motivo: MotivoCuenta, causa: Throwable? = null) : Exception(motivo.name, causa)

/**
 * La cuenta es opcional y solo habilita el respaldo en la nube y el resumen con IA. Las operaciones devuelven
 * [Result]: en caso de falla, la excepción es una [CuentaException].
 */
interface SesionRepository {

    /** La sesión actual; se conserva entre aperturas de la app. */
    val sesion: Flow<EstadoSesion>

    suspend fun iniciarSesion(email: String, clave: String): Result<Usuario>

    suspend fun crearCuenta(email: String, clave: String): Result<Usuario>

    /** Envía el email para elegir una contraseña nueva (el enlace vuelve a abrir la app con un deep link). */
    suspend fun recuperarContrasena(email: String): Result<Unit>

    /** Para quien ya inició sesión, o llegó desde el enlace del email. */
    suspend fun cambiarContrasena(nueva: String): Result<Unit>

    suspend fun cerrarSesion()

    /** Borra la cuenta y su respaldo en la nube. Los vinos del teléfono no se tocan. */
    suspend fun eliminarCuenta(): Result<Unit>
}
