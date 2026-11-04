// Capa de datos · ML Kit: lee en el teléfono el texto de la foto de la etiqueta. Sin red ni cuenta; la foto no sale del dispositivo.
package com.sorbo.app.data.ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.TextoEtiqueta
import com.sorbo.app.domain.repository.LectorEtiqueta
import com.sorbo.app.domain.repository.LecturaEtiquetaException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Usa el modelo de reconocimiento de texto que viene incluido en la app: funciona sin conexión.
 * Lee la foto derecha y además girada 90° a cada lado, porque muchas botellas se sacan acostadas y el texto
 * de lado se lee peor; las lecturas giradas solo completan lo que la derecha no encontró.
 */
class LectorEtiquetaMlKit : LectorEtiqueta {

    override suspend fun leer(fotoPath: String): TextoEtiqueta {
        val mapaDeBits = withContext(Dispatchers.IO) {
            // La foto ya viene achicada (≤ 500 KB) y derecha, así que no hace falta enderezarla.
            BitmapFactory.decodeFile(fotoPath) ?: throw LecturaEtiquetaException("No se pudo abrir la foto")
        }
        val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val derecha = leer(reconocedor, mapaDeBits, grados = 0)
            val alternativas = GIROS.map { grados ->
                // Si una lectura girada falla, se sigue sin ella: la derecha ya alcanza para sugerir algo.
                try {
                    leer(reconocedor, mapaDeBits, grados).bloques
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (falla: LecturaEtiquetaException) {
                    emptyList()
                }
            }
            return TextoEtiqueta(bloques = derecha.bloques, alternativas = alternativas)
        } finally {
            reconocedor.close()
        }
    }

    private suspend fun leer(reconocedor: TextRecognizer, mapaDeBits: Bitmap, grados: Int): TextoEtiqueta =
        suspendCancellableCoroutine { continuacion ->
            reconocedor.process(InputImage.fromBitmap(mapaDeBits, grados))
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

    private companion object {
        val GIROS = listOf(90, 270)
    }
}
