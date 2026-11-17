// Capa de datos · lectura de etiquetas en la nube: implementación de LectorEtiquetaNube con la función leer-etiqueta.
package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.LecturaNube
import com.sorbo.app.domain.repository.LectorEtiquetaNube
import kotlinx.coroutines.CancellationException
import java.io.File
import java.util.Base64

/**
 * Manda la foto de la etiqueta a la función de Alejo, que la lee con un modelo de visión (la clave de Groq nunca está en la app).
 * El token lo pone [token] (la sesión), que lo renueva si venció. La foto solo se envía si [com.sorbo.app.domain.usecase.PuedeLeerEnLaNube] lo permite.
 */
class LectorEtiquetaNubeSupabase(
    private val api: SupabaseFuncionesApi,
    private val token: ProveedorDeToken,
    private val leerArchivo: (String) -> ByteArray = { File(it).readBytes() },
) : LectorEtiquetaNube {

    override suspend fun leer(fotoPath: String): Result<LecturaNube> = try {
        val bytes = leerArchivo(fotoPath)
        if (bytes.size > MAXIMO_BYTES) {
            Result.failure(IllegalArgumentException("La foto es demasiado grande para enviarla"))
        } else {
            val pedido = PedidoEtiquetaDto(Base64.getEncoder().encodeToString(bytes))
            val respuesta = token.conToken { t -> api.leerEtiqueta("Bearer $t", pedido) }
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                Result.success(cuerpo.aLectura())
            } else {
                Result.failure(IllegalStateException("La lectura en la nube respondió ${respuesta.code()}"))
            }
        }
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (falla: Exception) {
        Result.failure(falla)
    }

    companion object {
        /** La función acepta hasta 1.000.000 de caracteres en base64, que son unos 750.000 bytes. */
        const val MAXIMO_BYTES = 700_000
    }
}
