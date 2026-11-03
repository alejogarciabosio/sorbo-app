// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

import java.time.LocalDate

/** Un vino con sus degustaciones (sin las eliminadas). Es lo que muestran el diario y el detalle. */
data class VinoConDegustaciones(
    val vino: Vino,
    val degustaciones: List<Degustacion>,
) {
    val veces: Int get() = degustaciones.size

    val ultimaFecha: LocalDate? get() = degustaciones.maxOfOrNull { it.fecha }

    /** Gustó en la mitad o más de las veces que se probó. */
    val gustoEnGeneral: Boolean get() = veces == 0 || degustaciones.count { it.meGusto } * 2 >= veces
}
