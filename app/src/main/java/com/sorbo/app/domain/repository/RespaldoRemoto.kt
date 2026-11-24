// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Vino

/**
 * La copia del diario en la nube (Supabase). Es un puerto del dominio: el caso de uso de sincronización
 * depende de esta interfaz y no de Retrofit, así se prueba con la versión en memoria.
 * Guardar es un *upsert*: si el registro ya existe (misma id) se reemplaza, nunca se duplica.
 * Cualquier falla es una [RespaldoException].
 */
interface RespaldoRemoto {

    /** Sube la foto de la etiqueta y devuelve su ruta en la nube (`{usuarioId}/{vinoId}.jpg`). */
    suspend fun subirFoto(usuarioId: String, vinoId: String, rutaLocal: String): String

    suspend fun guardarVinos(vinos: List<Vino>)

    suspend fun guardarDegustaciones(degustaciones: List<Degustacion>)

    /** Si el usuario tiene vinos (no eliminados) en la nube: sirve para ofrecer restaurar en un teléfono nuevo. */
    suspend fun hayRespaldo(usuarioId: String): Boolean

    // --- Restauración: traer el diario a un teléfono nuevo ---

    /** Cuántos vinos tiene el usuario en la nube (incluidos los eliminados): es el total del progreso. */
    suspend fun contarVinos(usuarioId: String): Int

    /** Una página de vinos del usuario, ordenados por `id` para que las páginas no se pisen. */
    suspend fun bajarVinos(usuarioId: String, desde: Int, cantidad: Int): List<Vino>

    suspend fun bajarDegustaciones(usuarioId: String, desde: Int, cantidad: Int): List<Degustacion>

    /** Los bytes de una foto subida con [subirFoto] (su ruta en la nube es la `fotoUrl` del vino). */
    suspend fun bajarFoto(rutaRemota: String): ByteArray
}

/**
 * Falla del respaldo. [reintentable] es `true` cuando vale la pena volver a probar más tarde
 * (sin conexión, servidor caído) y `false` cuando reintentar no lo arregla (por ejemplo, un dato rechazado).
 */
class RespaldoException(val reintentable: Boolean, mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)
