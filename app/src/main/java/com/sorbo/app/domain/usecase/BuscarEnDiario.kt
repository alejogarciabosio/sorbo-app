// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.util.extraerAnio
import com.sorbo.app.domain.util.normalizar
import com.sorbo.app.domain.util.quitarAnio

sealed interface ResultadoBusqueda {
    data class Encontrados(val vinos: List<VinoConDegustaciones>) : ResultadoBusqueda

    /** No hay esa añada, pero sí otras del mismo vino. */
    data class SoloOtraAnada(val vinos: List<VinoConDegustaciones>, val anioBuscado: Int) : ResultadoBusqueda

    /** El diario no lo tiene. No significa que el usuario nunca lo haya probado: puede no haberlo cargado. */
    data class NoEncontrado(val consulta: String) : ResultadoBusqueda
}

/** Busca por nombre, bodega, cepa o año, sin tildes ni mayúsculas y con texto parcial (RF02). No usa la red. */
class BuscarEnDiario(private val repositorio: VinoRepository) {

    suspend operator fun invoke(consulta: String): ResultadoBusqueda {
        val normalizada = normalizar(consulta)
        if (normalizada.isEmpty()) return ResultadoBusqueda.Encontrados(emptyList())

        val directos = repositorio.buscar(normalizada)
        if (directos.isNotEmpty()) return ResultadoBusqueda.Encontrados(directos)

        val anio = extraerAnio(consulta)
        if (anio != null) {
            val sinAnio = normalizar(quitarAnio(consulta))
            if (sinAnio.isNotEmpty()) {
                val otrasAnadas = repositorio.buscar(sinAnio)
                if (otrasAnadas.isNotEmpty()) return ResultadoBusqueda.SoloOtraAnada(otrasAnadas, anio)
            }
        }
        return ResultadoBusqueda.NoEncontrado(consulta.trim())
    }
}
