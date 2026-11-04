// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.VinoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DetalleVino(
    val vino: Vino,
    /** De la más reciente a la más antigua. */
    val degustaciones: List<Degustacion>,
    val otrasAnadas: List<Vino>,
) {
    val veces: Int get() = degustaciones.size

    val vecesQueGusto: Int get() = degustaciones.count { it.meGusto }

    /** Gustó en la mitad o más de las veces que se probó (igual que en el diario). */
    val gustoEnGeneral: Boolean get() = veces == 0 || vecesQueGusto * 2 >= veces

    /** El dulzor que más se repite entre las degustaciones; si hay empate o ninguna lo indicó, null. */
    val dulzorHabitual: Dulzor?
        get() {
            val cantidades = degustaciones.mapNotNull { it.dulzor }.groupingBy { it }.eachCount()
            val mayor = cantidades.values.maxOrNull() ?: return null
            return cantidades.entries.singleOrNull { it.value == mayor }?.key
        }
}

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
