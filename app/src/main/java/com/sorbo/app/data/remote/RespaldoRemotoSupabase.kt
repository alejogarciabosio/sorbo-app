// Capa de datos · respaldo real en Supabase: implementación de RespaldoRemoto con Retrofit (tablas y fotos).
package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.repository.RespaldoRemoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException

/**
 * Todos los pedidos van con el token del usuario a través de [token] (lo renueva si venció).
 * Traduce cada falla a una [RespaldoException] que dice si vale la pena reintentar:
 * sin red, servidor caído o "demasiados pedidos" → sí; un dato rechazado (4xx) o la sesión perdida → no.
 */
class RespaldoRemotoSupabase(
    private val api: SupabaseRestApi,
    private val token: ProveedorDeToken,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RespaldoRemoto {

    override suspend fun subirFoto(usuarioId: String, vinoId: String, rutaLocal: String): String {
        val ruta = "$usuarioId/$vinoId.jpg"
        val bytes = withContext(dispatcher) {
            try {
                File(rutaLocal).readBytes()
            } catch (e: IOException) {
                // La foto local ya no está: reintentar no la va a hacer aparecer.
                throw RespaldoException(reintentable = false, mensaje = "No se pudo leer la foto", causa = e)
            }
        }
        val cuerpo = bytes.toRequestBody(TIPO_JPEG)
        pedir { t -> api.subirFoto(bearer(t), ruta, cuerpo) }
        return ruta
    }

    override suspend fun guardarVinos(vinos: List<Vino>) {
        pedir { t -> api.guardarVinos(bearer(t), vinos.map { it.aDto() }) }
    }

    override suspend fun guardarDegustaciones(degustaciones: List<Degustacion>) {
        pedir { t -> api.guardarDegustaciones(bearer(t), degustaciones.map { it.aDto() }) }
    }

    override suspend fun hayRespaldo(usuarioId: String): Boolean =
        pedir { t -> api.buscarIdsDeVinos(bearer(t), usuarioId = "eq.$usuarioId") }.orEmpty().isNotEmpty()

    /** Hace el pedido con el token y devuelve el cuerpo; cualquier falla sale como [RespaldoException]. */
    private suspend fun <T> pedir(llamada: suspend (String) -> Response<T>): T? {
        val respuesta = try {
            token.conToken(llamada)
        } catch (e: CancellationException) {
            throw e
        } catch (e: CuentaException) {
            throw RespaldoException(reintentable = false, mensaje = "Sin sesión", causa = e)
        } catch (e: IOException) {
            throw RespaldoException(reintentable = true, mensaje = "Sin conexión", causa = e)
        }
        if (!respuesta.isSuccessful) {
            throw RespaldoException(reintentable = esReintentable(respuesta.code()), mensaje = "HTTP ${respuesta.code()}")
        }
        return respuesta.body()
    }

    private fun bearer(t: String) = "Bearer $t"

    companion object {
        private val TIPO_JPEG = "image/jpeg".toMediaType()

        /** 5xx (servidor), 408 (tiempo agotado) y 429 (demasiados pedidos) se arreglan esperando. */
        fun esReintentable(codigoHttp: Int): Boolean = codigoHttp >= 500 || codigoHttp == 408 || codigoHttp == 429
    }
}
