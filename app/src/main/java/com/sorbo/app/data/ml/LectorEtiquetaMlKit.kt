// Capa de datos · ML Kit: lee en el teléfono el texto de la foto de la etiqueta. Sin red ni cuenta; la foto no sale del dispositivo.
package com.sorbo.app.data.ml

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.TextoEtiqueta
import com.sorbo.app.domain.repository.LectorEtiqueta
import com.sorbo.app.domain.repository.LecturaEtiquetaException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Usa el modelo de reconocimiento de texto que viene incluido en la app: funciona sin conexión. */
class LectorEtiquetaMlKit : LectorEtiqueta {

    override suspend fun leer(fotoPath: String): TextoEtiqueta {
        val imagen = withContext(Dispatchers.IO) {
            // La foto ya viene achicada (≤ 500 KB) y derecha, así que no hace falta rotarla.
            val mapaDeBits = BitmapFactory.decodeFile(fotoPath)
                ?: throw LecturaEtiquetaException("No se pudo abrir la foto")
            InputImage.fromBitmap(mapaDeBits, 0)
        }
        val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            return suspendCancellableCoroutine { continuacion ->
                reconocedor.process(imagen)
                    .addOnSuccessListener { resultado ->
                        // Un bloque por renglón: el alto de cada renglón indica qué tan grande es la letra.
                        val bloques = resultado.textBlocks
                            .flatMap { it.lines }
                            .mapNotNull { renglon ->
                                renglon.boundingBox?.let { BloqueTexto(renglon.text, it.height().toFloat()) }
                            }
                        continuacion.resume(TextoEtiqueta(bloques))
                    }
                    .addOnFailureListener { falla ->
                        continuacion.resumeWithException(LecturaEtiquetaException("No se pudo leer la etiqueta", falla))
                    }
            }
        } finally {
            reconocedor.close()
        }
    }
}
