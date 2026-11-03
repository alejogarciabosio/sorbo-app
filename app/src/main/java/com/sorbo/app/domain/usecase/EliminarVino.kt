// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.repository.VinoRepository

/** Borrado lógico del vino y de sus degustaciones, con posibilidad de deshacer. */
class EliminarVino(private val repositorio: VinoRepository) {

    suspend operator fun invoke(vinoId: String) = repositorio.eliminarVino(vinoId)

    suspend fun deshacer(vinoId: String) = repositorio.restaurarVino(vinoId)
}
