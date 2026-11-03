// Capa de datos · almacén de fotos: copia, endereza y achica las fotos de las etiquetas en el almacenamiento interno.
package com.sorbo.app.data.foto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.util.LADOS_MAXIMOS_FOTO_PX
import com.sorbo.app.domain.util.comprimirHastaTamano
import com.sorbo.app.domain.util.medidasParaLadoMaximo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID

/**
 * Guarda las fotos en `filesDir/fotos` como JPEG de a lo sumo 500 KB. Sirve igual para la foto de la cámara
 * y para la de la galería: ninguna sale del teléfono.
 */
class AlmacenFotosLocal(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AlmacenFotos {

    private val carpeta: File get() = File(context.filesDir, CARPETA).apply { mkdirs() }

    override suspend fun guardar(origen: String): Result<String> = withContext(dispatcher) {
        try {
            val uri = Uri.parse(origen)
            val archivo = File(carpeta, "${UUID.randomUUID()}.jpg")
            archivo.writeBytes(achicar(uri))
            borrarSiEsDeLaCamara(uri)
            Result.success(archivo.absolutePath)
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override fun borrar(ruta: String) {
        val archivo = File(ruta)
        if (archivo.parentFile?.canonicalFile == carpeta.canonicalFile) archivo.delete()
    }

    /** Lee la foto, la endereza y prueba tamaños cada vez más chicos hasta que pese 500 KB o menos. */
    private fun achicar(uri: Uri): ByteArray {
        val original = decodificar(uri)
        try {
            for (lado in LADOS_MAXIMOS_FOTO_PX) {
                val medidas = medidasParaLadoMaximo(original.width, original.height, lado)
                val reducida = if (medidas.ancho == original.width && medidas.alto == original.height) {
                    original
                } else {
                    Bitmap.createScaledBitmap(original, medidas.ancho, medidas.alto, true)
                }
                val bytes = comprimirHastaTamano { calidad -> reducida.aJpeg(calidad) }
                if (reducida !== original) reducida.recycle()
                if (bytes != null) return bytes
            }
        } finally {
            original.recycle()
        }
        error("La foto no entra en el peso máximo")
    }

    private fun decodificar(uri: Uri): Bitmap {
        val medidas = abrir(uri).use { entrada ->
            val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(entrada, null, opciones)
            opciones
        }
        // Lee la foto ya reducida a una potencia de dos, para no llenar la memoria con una foto de 12 megapíxeles.
        var muestreo = 1
        while (maxOf(medidas.outWidth, medidas.outHeight) / (muestreo * 2) >= LADOS_MAXIMOS_FOTO_PX.first()) muestreo *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val bitmap = abrir(uri).use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: error("No se pudo leer la foto")
        val grados = abrir(uri).use { gradosDeRotacion(it) }
        return if (grados == 0) bitmap else rotar(bitmap, grados)
    }

    private fun gradosDeRotacion(entrada: InputStream): Int =
        when (ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun rotar(bitmap: Bitmap, grados: Int): Bitmap {
        val matriz = Matrix().apply { postRotate(grados.toFloat()) }
        val rotado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
        bitmap.recycle()
        return rotado
    }

    private fun Bitmap.aJpeg(calidad: Int): ByteArray =
        ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.JPEG, calidad, it) }.toByteArray()

    private fun abrir(uri: Uri): InputStream =
        context.contentResolver.openInputStream(uri) ?: error("No se pudo abrir la foto")

    /** La foto temporal de la cámara ya no hace falta una vez guardada la copia. */
    private fun borrarSiEsDeLaCamara(uri: Uri) {
        if (uri.authority == autoridadDeArchivos(context)) {
            runCatching { context.contentResolver.delete(uri, null, null) }
        }
    }

    companion object {
        private const val CARPETA = "fotos"

        /** Autoridad del FileProvider que declara el manifest. */
        fun autoridadDeArchivos(context: Context) = "${context.packageName}.fileprovider"
    }
}
