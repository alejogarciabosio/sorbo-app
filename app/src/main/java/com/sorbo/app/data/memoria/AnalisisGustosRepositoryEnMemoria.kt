// Capa de datos · resumen con IA simulado: devuelve un texto fijo para construir las pantallas sin la Edge Function.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.model.AnalisisGustos
import com.sorbo.app.domain.repository.AnalisisException
import com.sorbo.app.domain.repository.AnalisisGustosRepository
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock

/** [fallaConMotivo] permite simular un error (sin conexión, límite alcanzado...) para probar esos estados de pantalla. */
class AnalisisGustosRepositoryEnMemoria(
    private val reloj: Clock = Clock.systemUTC(),
    var fallaConMotivo: MotivoAnalisis? = null,
) : AnalisisGustosRepository {

    private val ultimo = MutableStateFlow<AnalisisGustos?>(null)

    override fun observarUltimo(): Flow<AnalisisGustos?> = ultimo.asStateFlow()

    override suspend fun generar(estadisticas: EstadisticasGustos): Result<AnalisisGustos> {
        fallaConMotivo?.let { return Result.failure(AnalisisException(it)) }
        val analisis = AnalisisGustos(
            resumen = "Resumen de ejemplo basado en ${estadisticas.totalDegustaciones} degustaciones.",
            sugerencias = listOf("Probá un vino parecido a los que más te gustaron."),
            generadoEn = reloj.instant(),
            cantidadDegustaciones = estadisticas.totalDegustaciones,
        )
        ultimo.value = analisis
        return Result.success(analisis)
    }
}
