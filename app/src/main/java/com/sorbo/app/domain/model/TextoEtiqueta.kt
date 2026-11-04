// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

/** Un renglón o bloque de texto leído de la foto. [altura] es el tamaño relativo de las letras: mayor significa más grande. */
data class BloqueTexto(val texto: String, val altura: Float)

/**
 * @property bloques lo que se leyó de la foto derecha.
 * @property alternativas el mismo texto leído con la foto girada (botellas sostenidas de costado): sirve para completar
 * lo que la lectura principal no encontró (año, cepa, bodega), nunca para pisar lo que ya tiene.
 */
data class TextoEtiqueta(val bloques: List<BloqueTexto>, val alternativas: List<List<BloqueTexto>> = emptyList())

/** Lo que la lectura de la etiqueta propone para el formulario. Nunca se guarda sin que el usuario confirme. */
data class SugerenciaEtiqueta(
    val nombre: String? = null,
    val bodega: String? = null,
    val cepa: String? = null,
    val anio: Int? = null,
) {
    val estaVacia: Boolean get() = nombre == null && bodega == null && cepa == null && anio == null
}
