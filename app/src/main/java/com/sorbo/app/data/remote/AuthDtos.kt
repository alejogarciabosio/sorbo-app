// Capa de datos · DTOs de Supabase Auth: la forma exacta del JSON que se envía y se recibe. No salen de data/remote.
package com.sorbo.app.data.remote

import com.google.gson.annotations.SerializedName

/** Cuerpo para crear cuenta e iniciar sesión. */
data class CredencialesDto(
    val email: String,
    val password: String,
)

data class RenovarSesionDto(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class EmailDto(
    val email: String,
)

data class ClaveNuevaDto(
    val password: String,
)

data class UsuarioDto(
    val id: String,
    val email: String?,
)

/**
 * Respuesta de iniciar sesión, renovarla y crear cuenta. Si Supabase pide confirmar el email,
 * crear cuenta no trae tokens: devuelve solo el usuario (por eso todo puede venir vacío).
 */
data class SesionDto(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiraEnSegundos: Long?,
    val user: UsuarioDto?,
    // Cuando no hay sesión, el usuario viene en la raíz del JSON.
    val id: String?,
    val email: String?,
)

/** Error de Supabase Auth. Según la versión del servidor el texto llega en uno u otro campo. */
data class ErrorAuthDto(
    @SerializedName("error_code") val codigo: String?,
    val error: String?,
    @SerializedName("error_description") val descripcion: String?,
    val msg: String?,
)
