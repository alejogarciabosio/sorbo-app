// Capa de datos · sesión simulada en memoria: permite construir y probar las pantallas de cuenta sin Supabase.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.util.esClaveValida
import com.sorbo.app.domain.util.esEmailValido
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Guarda las cuentas en memoria y respeta el mismo contrato que la implementación con Supabase.
 * [hayConexion] permite simular que no hay red (las operaciones fallan con SIN_CONEXION).
 */
class SesionRepositoryEnMemoria(var hayConexion: Boolean = true) : SesionRepository {

    private val cuentas = mutableMapOf<String, Pair<Usuario, String>>()
    private val _sesion = MutableStateFlow<EstadoSesion>(EstadoSesion.SinSesion)
    override val sesion: Flow<EstadoSesion> = _sesion.asStateFlow()

    override suspend fun iniciarSesion(email: String, clave: String): Result<Usuario> {
        if (!hayConexion) return falla(MotivoCuenta.SIN_CONEXION)
        val cuenta = cuentas[email.trim().lowercase()]
        if (cuenta == null || cuenta.second != clave) return falla(MotivoCuenta.CREDENCIALES_INCORRECTAS)
        _sesion.value = EstadoSesion.ConSesion(cuenta.first)
        return Result.success(cuenta.first)
    }

    override suspend fun crearCuenta(email: String, clave: String): Result<Usuario> {
        val correo = email.trim().lowercase()
        if (!esEmailValido(correo)) return falla(MotivoCuenta.EMAIL_INVALIDO)
        if (!esClaveValida(clave)) return falla(MotivoCuenta.CLAVE_DEBIL)
        if (!hayConexion) return falla(MotivoCuenta.SIN_CONEXION)
        if (correo in cuentas) return falla(MotivoCuenta.EMAIL_YA_REGISTRADO)
        val usuario = Usuario(id = UUID.randomUUID().toString(), email = correo)
        cuentas[correo] = usuario to clave
        _sesion.value = EstadoSesion.ConSesion(usuario)
        return Result.success(usuario)
    }

    override suspend fun recuperarContrasena(email: String): Result<Unit> {
        if (!esEmailValido(email.trim())) return falla(MotivoCuenta.EMAIL_INVALIDO)
        if (!hayConexion) return falla(MotivoCuenta.SIN_CONEXION)
        // Igual que en Supabase: no se revela si el email tiene cuenta o no.
        return Result.success(Unit)
    }

    override suspend fun cambiarContrasena(nueva: String): Result<Unit> {
        val actual = _sesion.value as? EstadoSesion.ConSesion ?: return falla(MotivoCuenta.SIN_SESION)
        if (!esClaveValida(nueva)) return falla(MotivoCuenta.CLAVE_DEBIL)
        if (!hayConexion) return falla(MotivoCuenta.SIN_CONEXION)
        cuentas[actual.usuario.email] = actual.usuario to nueva
        return Result.success(Unit)
    }

    override suspend fun cerrarSesion() {
        _sesion.value = EstadoSesion.SinSesion
    }

    override suspend fun eliminarCuenta(): Result<Unit> {
        val actual = _sesion.value as? EstadoSesion.ConSesion ?: return falla(MotivoCuenta.SIN_SESION)
        if (!hayConexion) return falla(MotivoCuenta.SIN_CONEXION)
        cuentas.remove(actual.usuario.email)
        _sesion.value = EstadoSesion.SinSesion
        return Result.success(Unit)
    }

    private fun <T> falla(motivo: MotivoCuenta): Result<T> = Result.failure(CuentaException(motivo))
}
