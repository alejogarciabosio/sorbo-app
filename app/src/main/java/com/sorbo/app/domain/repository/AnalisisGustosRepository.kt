// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.AnalisisGustos
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.flow.Flow

enum class MotivoAnalisis {
    SIN_CONEXION,
    SIN_SESION,
    LIMITE_ALCANZADO,
    SERVICIO_NO_DISPONIBLE,
    ERROR_DESCONOCIDO,
}

class AnalisisException(val motivo: MotivoAnalisis, causa: Throwable? = null) : Exception(motivo.name, causa)

/**
 * El resumen de gustos con IA (RF04). Solo se envían las estadísticas, nunca fotos ni notas (RNF06).
 * El último resumen queda guardado en el teléfono para verlo sin conexión.
 */
interface AnalisisGustosRepository {

    /** El último resumen guardado, o null si todavía no se generó ninguno. */
    fun observarUltimo(): Flow<AnalisisGustos?>

    /**
     * Pide un resumen nuevo (necesita conexión y sesión), lo guarda y lo emite por [observarUltimo].
     * En caso de falla, el resumen anterior se conserva y la excepción es un [AnalisisException].
     */
    suspend fun generar(estadisticas: EstadisticasGustos): Result<AnalisisGustos>
}
