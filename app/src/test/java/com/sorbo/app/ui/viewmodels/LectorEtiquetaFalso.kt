// Pruebas · doble de prueba: lector de etiquetas que devuelve lo que el test le indique, sin ML Kit.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.TextoEtiqueta
import com.sorbo.app.domain.repository.LectorEtiqueta
import com.sorbo.app.domain.repository.LecturaEtiquetaException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** Una etiqueta clara: "Rutini Malbec" grande, con la bodega y el año. */
internal val ETIQUETA_RUTINI = TextoEtiqueta(
    listOf(
        BloqueTexto("RUTINI", 80f),
        BloqueTexto("MALBEC", 50f),
        BloqueTexto("2021", 30f),
        BloqueTexto("Rutini Wines", 20f),
    ),
)

internal val ETIQUETA_OTRA = TextoEtiqueta(
    listOf(BloqueTexto("ZUCCARDI", 80f), BloqueTexto("Bodega Zuccardi", 20f), BloqueTexto("2019", 30f)),
)

internal val ETIQUETA_SIN_TEXTO = TextoEtiqueta(emptyList())

/** Lector que responde lo que indique el test; si [puerta] tiene valor, espera a que se complete para responder. */
internal class LectorEtiquetaFalso : LectorEtiqueta {
    var texto: TextoEtiqueta = ETIQUETA_RUTINI
    var falla: Boolean = false
    var puerta: CompletableDeferred<Unit>? = null
    val leidas = mutableListOf<String>()

    override suspend fun leer(fotoPath: String): TextoEtiqueta {
        leidas += fotoPath
        val respuesta = texto // se toma al empezar: cada lectura responde lo que había cuando arrancó
        // No cancelable a propósito: así se prueba que el ViewModel descarta por sí mismo el resultado de una foto vieja.
        puerta?.let { withContext(NonCancellable) { it.await() } }
        if (falla) throw LecturaEtiquetaException("no se pudo leer")
        return respuesta
    }
}
