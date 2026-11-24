// Capa de dominio · utilidad: lee los tokens del enlace sorbo://recuperar que llega por mail, sin depender de Android.
package com.sorbo.app.domain.util

/** Los dos tokens que trae el enlace de recuperar contraseña. */
data class TokensDeEnlace(val accessToken: String, val refreshToken: String)

/**
 * Supabase vuelve a la app con los tokens en el fragmento: `sorbo://recuperar#access_token=...&refresh_token=...&type=recovery`.
 * Devuelve null si falta alguno, si el enlace no es de recuperación o si trae un error (por ejemplo `error_code=otp_expired`
 * cuando el enlace venció): en esos casos la persona tiene que pedir uno nuevo.
 */
fun tokensDelEnlace(fragmento: String?): TokensDeEnlace? {
    if (fragmento.isNullOrBlank()) return null
    val datos = fragmento.split('&').mapNotNull { par ->
        val partes = par.split('=', limit = 2)
        if (partes.size == 2) partes[0] to java.net.URLDecoder.decode(partes[1], "UTF-8") else null
    }.toMap()
    if ("error" in datos || "error_code" in datos) return null
    if (datos["type"] != null && datos["type"] != "recovery") return null
    val acceso = datos["access_token"]?.takeIf { it.isNotBlank() } ?: return null
    val renovacion = datos["refresh_token"]?.takeIf { it.isNotBlank() } ?: return null
    return TokensDeEnlace(acceso, renovacion)
}
