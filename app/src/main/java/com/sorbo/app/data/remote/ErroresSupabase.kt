// Capa de datos · traducción de errores: convierte las respuestas de error de Supabase Auth en un MotivoCuenta.
package com.sorbo.app.data.remote

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sorbo.app.domain.repository.MotivoCuenta
import retrofit2.Response
import java.io.IOException

private val gson = Gson()

/**
 * La UI solo conoce [MotivoCuenta]. Los códigos (`error_code`) son los que documenta Supabase Auth;
 * los servidores más viejos mandan `error` en lugar de `error_code`, por eso se miran los dos.
 */
fun motivoDeError(codigoHttp: Int, error: ErrorAuthDto?): MotivoCuenta {
    val codigo = error?.codigo ?: error?.error
    return when (codigo) {
        "invalid_credentials", "invalid_grant" -> MotivoCuenta.CREDENCIALES_INCORRECTAS
        "user_already_exists", "email_exists" -> MotivoCuenta.EMAIL_YA_REGISTRADO
        "weak_password" -> MotivoCuenta.CLAVE_DEBIL
        "email_address_invalid", "validation_failed" -> MotivoCuenta.EMAIL_INVALIDO
        "bad_jwt", "session_not_found", "session_expired", "refresh_token_not_found" -> MotivoCuenta.SIN_SESION
        else -> if (codigoHttp == 401) MotivoCuenta.SIN_SESION else MotivoCuenta.ERROR_DESCONOCIDO
    }
}

/** Lee el cuerpo de error de una respuesta fallida. Si no es el JSON esperado, devuelve null. */
fun leerError(respuesta: Response<*>): ErrorAuthDto? = try {
    respuesta.errorBody()?.string()?.let { gson.fromJson(it, ErrorAuthDto::class.java) }
} catch (e: JsonSyntaxException) {
    null
}

/** Una excepción al hacer el pedido: sin red (o se agotó el tiempo de espera) u otra cosa. */
fun motivoDeExcepcion(excepcion: Throwable): MotivoCuenta =
    if (excepcion is IOException) MotivoCuenta.SIN_CONEXION else MotivoCuenta.ERROR_DESCONOCIDO
