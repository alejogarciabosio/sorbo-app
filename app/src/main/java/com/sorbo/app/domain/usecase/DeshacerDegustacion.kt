// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.repository.VinoRepository

/** Quita la degustación que se acaba de sumar a un vino que ya estaba en el diario ("Sumamos esta vez a…" → "Deshacer"). */
class DeshacerDegustacion(private val repositorio: VinoRepository) {

    suspend operator fun invoke(degustacionId: String) = repositorio.eliminarDegustacion(degustacionId)
}
