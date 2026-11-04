// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

import java.time.Instant

/** El resumen de gustos escrito por la IA, junto con cuándo se generó y con cuántas degustaciones. */
data class AnalisisGustos(
    val resumen: String,
    val sugerencias: List<String>,
    val generadoEn: Instant,
    val cantidadDegustaciones: Int,
)
