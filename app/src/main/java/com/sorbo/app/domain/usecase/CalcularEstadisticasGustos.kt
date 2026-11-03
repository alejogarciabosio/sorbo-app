// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion

/** Qué se está midiendo en una fila de "Mis gustos". La pantalla lo traduce a texto ("Secos", "Con carne"). */
sealed interface CategoriaGusto {
    data class PorDulzor(val dulzor: Dulzor) : CategoriaGusto
    data class PorComida(val comida: Comida) : CategoriaGusto
    data class PorCompania(val compania: Compania) : CategoriaGusto
    data class PorOcasion(val ocasion: Ocasion) : CategoriaGusto
}

/** "Secos: 8 de 9": de [total] degustaciones de esa categoría, gustaron [favorables]. */
data class FilaGusto(val categoria: CategoriaGusto, val favorables: Int, val total: Int) {
    val proporcion: Float get() = if (total == 0) 0f else favorables.toFloat() / total
}

data class EstadisticasGustos(
    val totalDegustaciones: Int,
    /** Hay suficientes degustaciones para mostrar el análisis. */
    val habilitado: Boolean,
    /** Cuántas degustaciones faltan para habilitarlo. */
    val faltan: Int,
    val loQueMasDisfrutas: List<FilaGusto>,
    val loQueNoConvence: List<FilaGusto>,
)

/** Calcula las estadísticas en el teléfono, a partir del diario. Funciona sin conexión (RF04). */
class CalcularEstadisticasGustos {

    operator fun invoke(degustaciones: List<Degustacion>): EstadisticasGustos {
        val validas = degustaciones.filter { !it.eliminado }
        val total = validas.size
        if (total < MINIMO_DEGUSTACIONES) {
            return EstadisticasGustos(total, habilitado = false, faltan = MINIMO_DEGUSTACIONES - total, emptyList(), emptyList())
        }

        val filas = buildList {
            agruparPor(validas, { it.dulzor }) { add(FilaGusto(CategoriaGusto.PorDulzor(it.clave), it.favorables, it.total)) }
            agruparPor(validas, { it.comida?.takeIf { c -> c != Comida.OTRA && c != Comida.SIN_COMIDA } }) {
                add(FilaGusto(CategoriaGusto.PorComida(it.clave), it.favorables, it.total))
            }
            agruparPor(validas, { it.compania?.takeIf { c -> c != Compania.OTRA } }) {
                add(FilaGusto(CategoriaGusto.PorCompania(it.clave), it.favorables, it.total))
            }
            agruparPor(validas, { it.ocasion?.takeIf { o -> o != Ocasion.OTRA } }) {
                add(FilaGusto(CategoriaGusto.PorOcasion(it.clave), it.favorables, it.total))
            }
        }.filter { it.total >= MUESTRA_MINIMA }

        val ordenadas = filas.sortedWith(compareByDescending<FilaGusto> { it.proporcion }.thenByDescending { it.total })
        return EstadisticasGustos(
            totalDegustaciones = total,
            habilitado = true,
            faltan = 0,
            loQueMasDisfrutas = ordenadas.filter { it.proporcion >= UMBRAL_POSITIVO }.take(MAXIMO_FILAS),
            loQueNoConvence = ordenadas.filter { it.proporcion <= UMBRAL_NEGATIVO }.reversed().take(MAXIMO_FILAS),
        )
    }

    private class Grupo<K>(val clave: K, val favorables: Int, val total: Int)

    private inline fun <K : Any> agruparPor(
        degustaciones: List<Degustacion>,
        clave: (Degustacion) -> K?,
        agregar: (Grupo<K>) -> Unit,
    ) {
        degustaciones.groupBy(clave).forEach { (k, grupo) ->
            if (k != null) agregar(Grupo(k, grupo.count { it.meGusto }, grupo.size))
        }
    }

    companion object {
        const val MINIMO_DEGUSTACIONES = 5
        private const val MUESTRA_MINIMA = 2
        private const val UMBRAL_POSITIVO = 0.6f
        private const val UMBRAL_NEGATIVO = 0.4f
        private const val MAXIMO_FILAS = 5
    }
}
