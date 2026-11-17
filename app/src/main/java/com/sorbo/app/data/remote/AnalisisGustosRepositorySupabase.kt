// Capa de datos · resumen con IA real: implementación de AnalisisGustosRepository con la función analizar-gustos.
package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.AnalisisGustos
import com.sorbo.app.domain.repository.AnalisisException
import com.sorbo.app.domain.repository.AnalisisGustosRepository
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.time.Clock

/**
 * Manda solo las estadísticas (nunca fotos ni notas) a la función de Alejo y guarda el resumen en [almacen].
 * El token lo pone [token] (la sesión), que lo renueva si venció. Si algo falla, el resumen anterior queda.
 */
class AnalisisGustosRepositorySupabase(
    private val api: SupabaseFuncionesApi,
    private val token: ProveedorDeToken,
    private val almacen: AlmacenAnalisis,
    private val reloj: Clock = Clock.systemUTC(),
) : AnalisisGustosRepository {

    private val ultimo = MutableStateFlow(almacen.leer())

    override fun observarUltimo(): Flow<AnalisisGustos?> = ultimo.asStateFlow()

    override suspend fun generar(estadisticas: EstadisticasGustos): Result<AnalisisGustos> = try {
        val respuesta = token.conToken { t -> api.analizarGustos("Bearer $t", estadisticas.aPedido()) }
        if (!respuesta.isSuccessful) {
            falla(motivoDeError(respuesta.code(), leerError(respuesta)?.error))
        } else {
            val cuerpo = respuesta.body()
            val resumen = cuerpo?.resumen?.trim().orEmpty()
            val sugerencias = cuerpo?.sugerencias.orEmpty().filter { it.isNotBlank() }
            if (resumen.isEmpty() || sugerencias.isEmpty()) {
                falla(MotivoAnalisis.ERROR_DESCONOCIDO)
            } else {
                val analisis = AnalisisGustos(resumen, sugerencias, reloj.instant(), estadisticas.totalDegustaciones)
                almacen.guardar(analisis)
                ultimo.value = analisis
                Result.success(analisis)
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: CuentaException) {
        // Sin sesión o con la sesión vencida sin poder renovarse.
        val motivo = if (e.motivo == MotivoCuenta.SIN_SESION) MotivoAnalisis.SIN_SESION else MotivoAnalisis.ERROR_DESCONOCIDO
        Result.failure(AnalisisException(motivo, e))
    } catch (e: IOException) {
        Result.failure(AnalisisException(MotivoAnalisis.SIN_CONEXION, e))
    } catch (e: Exception) {
        Result.failure(AnalisisException(MotivoAnalisis.ERROR_DESCONOCIDO, e))
    }

    private fun falla(motivo: MotivoAnalisis): Result<AnalisisGustos> = Result.failure(AnalisisException(motivo))

    companion object {
        /** Los códigos que devuelve la función (ver `05-diseno-ia.md`). */
        fun motivoDeError(codigoHttp: Int, codigo: String?): MotivoAnalisis = when {
            codigo == "limite" || codigoHttp == 429 -> MotivoAnalisis.LIMITE_ALCANZADO
            codigo == "sin_sesion" || codigoHttp == 401 -> MotivoAnalisis.SIN_SESION
            codigo == "servicio" || codigoHttp >= 500 -> MotivoAnalisis.SERVICIO_NO_DISPONIBLE
            else -> MotivoAnalisis.ERROR_DESCONOCIDO
        }
    }
}
