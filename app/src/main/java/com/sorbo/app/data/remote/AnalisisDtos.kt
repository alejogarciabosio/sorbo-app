// Capa de datos · DTOs de la función analizar-gustos: el JSON exacto que espera y devuelve la Edge Function de Alejo.
package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.usecase.CategoriaGusto
import com.sorbo.app.domain.usecase.EstadisticasGustos
import com.sorbo.app.domain.usecase.FilaGusto

/** Una fila de estadísticas: solo categorías y números, nunca notas ni nombres de vinos (RNF06). */
data class FilaGustoDto(
    val tipo: String,
    val valor: String,
    val favorables: Int,
    val total: Int,
)

data class PedidoAnalisisDto(
    val totalDegustaciones: Int,
    val disfrutas: List<FilaGustoDto>,
    val noConvence: List<FilaGustoDto>,
)

data class RespuestaAnalisisDto(
    val resumen: String?,
    val sugerencias: List<String>?,
)

/** La función acepta hasta 6 filas por lista. */
const val MAXIMO_FILAS_POR_LISTA = 6

/**
 * Proyecta las estadísticas al formato de la función. `OTRA` y `SIN_COMIDA` no se envían
 * (no dicen nada de un gusto); el cálculo ya los descarta, pero se filtran también acá por si eso cambia.
 */
fun EstadisticasGustos.aPedido(): PedidoAnalisisDto = PedidoAnalisisDto(
    totalDegustaciones = totalDegustaciones,
    disfrutas = loQueMasDisfrutas.mapNotNull { it.aDto() }.take(MAXIMO_FILAS_POR_LISTA),
    noConvence = loQueNoConvence.mapNotNull { it.aDto() }.take(MAXIMO_FILAS_POR_LISTA),
)

private fun FilaGusto.aDto(): FilaGustoDto? {
    val (tipo, valor) = when (val c = categoria) {
        is CategoriaGusto.PorDulzor -> "dulzor" to c.dulzor.name
        is CategoriaGusto.PorComida -> if (c.comida == Comida.OTRA || c.comida == Comida.SIN_COMIDA) return null else "comida" to c.comida.name
        is CategoriaGusto.PorCompania -> if (c.compania == Compania.OTRA) return null else "compania" to c.compania.name
        is CategoriaGusto.PorOcasion -> if (c.ocasion == Ocasion.OTRA) return null else "ocasion" to c.ocasion.name
    }
    return FilaGustoDto(tipo = tipo, valor = valor, favorables = favorables, total = total)
}
