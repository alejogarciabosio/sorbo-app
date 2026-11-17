// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

/**
 * Lo que todavía no está respaldado en la nube: vinos y degustaciones con estado PENDIENTE,
 * **incluidos los eliminados** (así una eliminación hecha sin conexión también se propaga).
 */
data class CambiosPendientes(
    val vinos: List<Vino>,
    val degustaciones: List<Degustacion>,
) {
    val estaVacio: Boolean get() = vinos.isEmpty() && degustaciones.isEmpty()
}
