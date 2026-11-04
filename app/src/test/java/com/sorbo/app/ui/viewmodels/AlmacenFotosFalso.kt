package com.sorbo.app.ui.viewmodels

import com.sorbo.app.domain.repository.AlmacenFotos
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException

/** Almacén que no toca el disco: anota qué fotos se guardaron y cuáles se borraron. */
internal class AlmacenFotosFalso(var falla: Boolean = false) : AlmacenFotos {
    val guardadas = mutableListOf<String>()
    val borradas = mutableListOf<String>()

    /** Si tiene valor, [guardar] espera a que se complete: sirve para probar el momento "procesando la foto". */
    var puerta: CompletableDeferred<Unit>? = null

    override suspend fun guardar(origen: String): Result<String> {
        puerta?.await()
        if (falla) return Result.failure(IOException("no se pudo leer"))
        return Result.success("fotos/${guardadas.size + 1}.jpg").also { guardadas += it.getOrThrow() }
    }

    override fun borrar(ruta: String) {
        borradas += ruta
    }
}
