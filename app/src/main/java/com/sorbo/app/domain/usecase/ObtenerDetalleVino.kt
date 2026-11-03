package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DetalleVino(
    val vino: Vino,
    /** De la más reciente a la más antigua. */
    val degustaciones: List<Degustacion>,
    val otrasAnadas: List<Vino>,
)

/** El vino con sus degustaciones y las otras añadas del mismo nombre ("Otras añadas que probaste"). */
class ObtenerDetalleVino(private val repositorio: VinoRepository) {

    operator fun invoke(vinoId: String): Flow<DetalleVino?> =
        combine(repositorio.observarVino(vinoId), repositorio.observarDiario()) { actual, diario ->
            actual?.let {
                DetalleVino(
                    vino = it.vino,
                    degustaciones = it.degustaciones.sortedByDescending { degustacion -> degustacion.fecha },
                    otrasAnadas = diario.map { otro -> otro.vino }
                        .filter { otro -> it.vino.esOtraAnadaDe(otro) }
                        .sortedBy { otro -> otro.anio },
                )
            }
        }
}
