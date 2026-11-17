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
}

/**
 * Falla del respaldo. [reintentable] es `true` cuando vale la pena volver a probar más tarde
 * (sin conexión, servidor caído) y `false` cuando reintentar no lo arregla (por ejemplo, un dato rechazado).
 */
class RespaldoException(val reintentable: Boolean, mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)
