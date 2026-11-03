// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.util.distanciaEntre

enum class TipoCoincidencia { MISMO_VINO, PARECIDO, NUEVO }

/**
 * @property vino el vino del diario con el que coincide (null si es nuevo).
 * @property otrasAnadas vinos del diario con el mismo nombre y bodega pero de otra añada.
 */
data class Coincidencia(
    val tipo: TipoCoincidencia,
    val vino: Vino?,
    val otrasAnadas: List<Vino>,
)

/**
 * Decide si un vino que se está cargando ya existe en el diario (regla de la sección 9 de la preentrega):
 * - Bodegas distintas o añadas distintas: son vinos distintos.
 * - Mismo nombre y misma añada (o ninguna cargada): es el mismo vino.
 * - Nombre parecido, o mismo nombre con la añada cargada solo en uno: parecido (la app pregunta).
 * - Nada parecido: vino nuevo.
 */
class BuscarVinoParecido(private val repositorio: VinoRepository) {

    suspend operator fun invoke(candidato: Vino): Coincidencia {
        val existentes = repositorio.listarVinos().filter { it.id != candidato.id }
        val otrasAnadas = mutableListOf<Vino>()
        var igual: Vino? = null
        var parecido: Vino? = null
        var menorDistancia = Int.MAX_VALUE

        for (existente in existentes) {
            if (candidato.tieneOtraBodegaQue(existente)) continue
            val nombresIguales = candidato.nombreNormalizado == existente.nombreNormalizado
            if (candidato.tieneOtraAnadaQue(existente)) {
                if (nombresIguales) otrasAnadas += existente
                continue
            }
            if (nombresIguales && candidato.anio == existente.anio) {
                if (igual == null) igual = existente
                continue
            }
            val distancia = if (nombresIguales) 0 else distanciaSiSeParecen(candidato.nombreNormalizado, existente.nombreNormalizado)
            if (distancia != null && distancia < menorDistancia) {
                parecido = existente
                menorDistancia = distancia
            }
        }

        return when {
            igual != null -> Coincidencia(TipoCoincidencia.MISMO_VINO, igual, otrasAnadas)
            parecido != null -> Coincidencia(TipoCoincidencia.PARECIDO, parecido, otrasAnadas)
            else -> Coincidencia(TipoCoincidencia.NUEVO, null, otrasAnadas)
        }
    }

    /** Distancia entre dos nombres distintos si se parecen; null si no. */
    private fun distanciaSiSeParecen(a: String, b: String): Int? {
        if (a.isEmpty() || b.isEmpty()) return null
        val menor = minOf(a.length, b.length)
        if (menor >= LARGO_MINIMO_CONTENIDO && (a.contains(b) || b.contains(a))) {
            return maxOf(1, kotlin.math.abs(a.length - b.length))
        }
        if (menor >= LARGO_MINIMO_TYPO) {
            val distancia = distanciaEntre(a, b)
            if (distancia <= DISTANCIA_MAXIMA) return distancia
        }
        return null
    }

    private companion object {
        const val LARGO_MINIMO_CONTENIDO = 3
        const val LARGO_MINIMO_TYPO = 5
        const val DISTANCIA_MAXIMA = 2
    }
}
