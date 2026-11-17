// Capa de datos · sesión con Supabase Auth: implementación real de SesionRepository usando la API REST.
package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.util.esClaveValida
import com.sorbo.app.domain.util.esEmailValido
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response

/**
 * Habla con Supabase Auth y guarda los tokens en [almacen] para que la sesión siga al reabrir la app.
 * Cumple el mismo contrato que `SesionRepositoryEnMemoria`: cada falla es un `Result.failure(CuentaException)`.
 * Las validaciones de email y contraseña se hacen antes de llamar a la red, con las mismas reglas que la pantalla.
 */
class SesionRepositorySupabase(
    private val api: SupabaseAuthApi,
    private val almacen: AlmacenSesion,
    private val enlaceRecuperar: String = ENLACE_RECUPERAR,
) : SesionRepository {

    private val _sesion = MutableStateFlow(estadoDe(almacen.leer()))
    override val sesion: Flow<EstadoSesion> = _sesion.asStateFlow()

    override suspend fun iniciarSesion(email: String, clave: String): Result<Usuario> =
        pedir({ api.iniciarSesion(CredencialesDto(email.trim().lowercase(), clave)) }) { respuesta ->
            guardar(respuesta) ?: throw CuentaException(MotivoCuenta.ERROR_DESCONOCIDO)
        }

    override suspend fun crearCuenta(email: String, clave: String): Result<Usuario> {
        val correo = email.trim().lowercase()
        if (!esEmailValido(correo)) return falla(MotivoCuenta.EMAIL_INVALIDO)
        if (!esClaveValida(clave)) return falla(MotivoCuenta.CLAVE_DEBIL)
        return pedir({ api.crearCuenta(CredencialesDto(correo, clave)) }) { respuesta ->
            // Si Supabase exige confirmar el email (decisión abierta #3), no llegan tokens: la cuenta existe
            // pero la sesión empieza recién cuando la persona confirma e inicia sesión.
            guardar(respuesta) ?: Usuario(id = respuesta.id ?: respuesta.user?.id.orEmpty(), email = correo)
        }
    }

    override suspend fun recuperarContrasena(email: String): Result<Unit> {
        val correo = email.trim().lowercase()
        if (!esEmailValido(correo)) return falla(MotivoCuenta.EMAIL_INVALIDO)
        // Supabase responde igual exista o no la cuenta: no se revela quién está registrado.
        return pedir({ api.recuperarContrasena(EmailDto(correo), enlaceRecuperar) }) { }
    }

    override suspend fun cambiarContrasena(nueva: String): Result<Unit> {
        if (almacen.leer() == null) return falla(MotivoCuenta.SIN_SESION)
        if (!esClaveValida(nueva)) return falla(MotivoCuenta.CLAVE_DEBIL)
        return pedir({ conToken { token -> api.cambiarContrasena(bearer(token), ClaveNuevaDto(nueva)) } }) { }
    }

    override suspend fun cerrarSesion() {
        val actual = almacen.leer()
        // Se avisa a Supabase si se puede, pero la sesión se cierra en el teléfono aunque no haya red.
        if (actual != null) runCatching { api.cerrarSesion(bearer(actual.accessToken)) }
        almacen.borrar()
        _sesion.value = EstadoSesion.SinSesion
    }

    override suspend fun eliminarCuenta(): Result<Unit> {
        if (almacen.leer() == null) return falla(MotivoCuenta.SIN_SESION)
        // Borrar la cuenta necesita la función `eliminar-cuenta` del servidor (tarea A5), que todavía no existe.
        return falla(MotivoCuenta.ERROR_DESCONOCIDO)
    }

    /**
     * Hace un pedido del usuario con su token. El token de acceso vence (por defecto, en una hora): si Supabase
     * dice que el token no sirve (ver [esTokenInvalido]), se renueva una sola vez y se reintenta. Si la renovación es
     * rechazada, la sesión ya no sirve: se cierra en el teléfono (`EstadoSesion.SinSesion`).
     */
    private suspend fun <C> conToken(llamada: suspend (String) -> Response<C>): Response<C> {
        val actual = almacen.leer() ?: throw CuentaException(MotivoCuenta.SIN_SESION)
        val primera = llamada(actual.accessToken)
        if (!esTokenInvalido(primera)) return primera

        val renovada = api.renovarSesion(RenovarSesionDto(actual.refreshToken))
        // Si el servidor falla (5xx) o no hay red, la sesión no se toca: puede andar en el próximo intento.
        if (renovada.code() >= HTTP_ERROR_DEL_SERVIDOR) throw CuentaException(MotivoCuenta.ERROR_DESCONOCIDO)
        if (!renovada.isSuccessful || renovada.body()?.let(::guardar) == null) {
            almacen.borrar()
            _sesion.value = EstadoSesion.SinSesion
            throw CuentaException(MotivoCuenta.SIN_SESION)
        }
        return llamada(almacen.leer()!!.accessToken)
    }

    /** Hace el pedido y traduce cualquier falla (HTTP o de red) a una [CuentaException]. */
    private suspend fun <C, T> pedir(llamada: suspend () -> Response<C>, alSalirBien: (C) -> T): Result<T> = try {
        val respuesta = llamada()
        if (respuesta.isSuccessful) {
            Result.success(alSalirBien(respuesta.body() as C))
        } else {
            falla(motivoDeError(respuesta.code(), leerError(respuesta)))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: CuentaException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(CuentaException(motivoDeExcepcion(e), e))
    }

    /** Si la respuesta trae tokens, los guarda y abre la sesión. Devuelve null si no los trae. */
    private fun guardar(respuesta: SesionDto): Usuario? {
        val access = respuesta.accessToken ?: return null
        val refresh = respuesta.refreshToken ?: return null
        val usuario = respuesta.user ?: return null
        val guardada = SesionGuardada(access, refresh, usuario.id, usuario.email.orEmpty())
        almacen.guardar(guardada)
        _sesion.value = estadoDe(guardada)
        return Usuario(usuario.id, usuario.email.orEmpty())
    }

    private fun estadoDe(guardada: SesionGuardada?): EstadoSesion =
        if (guardada == null) EstadoSesion.SinSesion else EstadoSesion.ConSesion(Usuario(guardada.usuarioId, guardada.email))

    private fun bearer(token: String) = "Bearer $token"

    private fun <T> falla(motivo: MotivoCuenta): Result<T> = Result.failure(CuentaException(motivo))

    companion object {
        /** Decisión abierta #4: el enlace del email de recuperación vuelve a abrir la app. */
        const val ENLACE_RECUPERAR = "sorbo://recuperar"

        private const val HTTP_ERROR_DEL_SERVIDOR = 500
    }
}
