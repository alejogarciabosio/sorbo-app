// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.SugerenciaEtiqueta
import com.sorbo.app.domain.repository.LectorEtiqueta
import kotlin.coroutines.cancellation.CancellationException

sealed interface ResultadoSugerencia {
    data class Exito(val sugerencia: SugerenciaEtiqueta) : ResultadoSugerencia

    /** La foto se leyó pero no se reconoció ningún dato. */
    data object SinDatos : ResultadoSugerencia

    /** No se pudo leer la foto. El usuario puede reintentar o cargar los datos a mano. */
    data class Error(val causa: Throwable) : ResultadoSugerencia
}

/** Lee la etiqueta en el teléfono y propone los datos del vino (RF03). No usa la red ni guarda nada. */
class SugerirDatosDesdeFoto(
    private val lector: LectorEtiqueta,
    private val interpretar: InterpretarTextoEtiqueta = InterpretarTextoEtiqueta(),
) {

    suspend operator fun invoke(fotoPath: String): ResultadoSugerencia =
        try {
            val sugerencia = interpretar(lector.leer(fotoPath))
            if (sugerencia.estaVacia) ResultadoSugerencia.SinDatos else ResultadoSugerencia.Exito(sugerencia)
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (falla: Exception) {
            ResultadoSugerencia.Error(falla)
        }
}
