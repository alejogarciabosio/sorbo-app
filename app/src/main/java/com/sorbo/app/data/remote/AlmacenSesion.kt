// Capa de datos · almacén de la sesión: guarda los tokens de Supabase en el teléfono para no pedir login en cada apertura.
package com.sorbo.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Lo mínimo para retomar la sesión al abrir la app. Nunca sale de la capa de datos. */
data class SesionGuardada(
    val accessToken: String,
    val refreshToken: String,
    val usuarioId: String,
    val email: String,
)

/** Interfaz para poder probar el repositorio con una versión en memoria. */
interface AlmacenSesion {
    fun leer(): SesionGuardada?
    fun guardar(sesion: SesionGuardada)
    fun borrar()
}

/**
 * Guarda la sesión en SharedPreferences en modo privado: solo esta app puede leer el archivo.
 * Se usa SharedPreferences (y no DataStore) porque son cuatro textos que se leen una vez al crear el repositorio.
 */
class AlmacenSesionPreferencias(context: Context) : AlmacenSesion {

    private val preferencias: SharedPreferences =
        context.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    override fun leer(): SesionGuardada? {
        val access = preferencias.getString(CLAVE_ACCESS, null) ?: return null
        val refresh = preferencias.getString(CLAVE_REFRESH, null) ?: return null
        val id = preferencias.getString(CLAVE_USUARIO_ID, null) ?: return null
        val email = preferencias.getString(CLAVE_EMAIL, null) ?: return null
        return SesionGuardada(access, refresh, id, email)
    }

    override fun guardar(sesion: SesionGuardada) {
        preferencias.edit {
            putString(CLAVE_ACCESS, sesion.accessToken)
            putString(CLAVE_REFRESH, sesion.refreshToken)
            putString(CLAVE_USUARIO_ID, sesion.usuarioId)
            putString(CLAVE_EMAIL, sesion.email)
        }
    }

    override fun borrar() {
        preferencias.edit { clear() }
    }

    private companion object {
        const val NOMBRE_ARCHIVO = "sesion_supabase"
        const val CLAVE_ACCESS = "access_token"
        const val CLAVE_REFRESH = "refresh_token"
        const val CLAVE_USUARIO_ID = "usuario_id"
        const val CLAVE_EMAIL = "email"
    }
}
