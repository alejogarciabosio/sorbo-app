// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

import java.time.Instant
import java.time.LocalDate

/** Una vez que se probó un vino, con la opinión y el contexto. */
data class Degustacion(
    val id: String,
    val vinoId: String,
    val fecha: LocalDate,
    val meGusto: Boolean,
    val actualizadoEn: Instant,
    val dulzor: Dulzor? = null,
    val comida: Comida? = null,
    val compania: Compania? = null,
    val ocasion: Ocasion? = null,
    val nota: String? = null,
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE,
    val eliminado: Boolean = false,
)
