// Capa de datos · almacén del resumen con IA: guarda el último resumen en el teléfono para verlo sin conexión.
package com.sorbo.app.data.remote

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sorbo.app.domain.model.AnalisisGustos
import java.time.Instant

/** Interfaz para poder probar el repositorio con una versión en memoria. */
interface AlmacenAnalisis {
    fun leer(): AnalisisGustos?
    fun guardar(analisis: AnalisisGustos)
}

/**
 * Guarda el resumen como JSON en SharedPreferences privadas. Es un solo registro que se reemplaza entero,
 * así que no hace falta una tabla de Room ni migrar la base.
 */
class AlmacenAnalisisPreferencias(context: Context) : AlmacenAnalisis {

    /** La forma guardada: la fecha como milisegundos, porque Gson no sabe convertir `Instant`. */
    private data class Guardado(
        val resumen: String,
        val sugerencias: List<String>,
        val generadoEnMilis: Long,
        val cantidadDegustaciones: Int,
    )

    private val preferencias = context.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun leer(): AnalisisGustos? {
        val json = preferencias.getString(CLAVE, null) ?: return null
        val guardado = try {
            gson.fromJson(json, Guardado::class.java)
        } catch (e: JsonSyntaxException) {
            return null
        } ?: return null
        return AnalisisGustos(
            resumen = guardado.resumen,
            sugerencias = guardado.sugerencias,
            generadoEn = Instant.ofEpochMilli(guardado.generadoEnMilis),
            cantidadDegustaciones = guardado.cantidadDegustaciones,
        )
    }

    override fun guardar(analisis: AnalisisGustos) {
        val guardado = Guardado(
            resumen = analisis.resumen,
            sugerencias = analisis.sugerencias,
            generadoEnMilis = analisis.generadoEn.toEpochMilli(),
            cantidadDegustaciones = analisis.cantidadDegustaciones,
        )
        preferencias.edit { putString(CLAVE, gson.toJson(guardado)) }
    }

    private companion object {
        const val NOMBRE_ARCHIVO = "analisis_gustos"
        const val CLAVE = "ultimo"
    }
}
