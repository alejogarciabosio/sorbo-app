// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

/**
 * Lo que devolvió el modelo de visión de la nube al leer la etiqueta. [lineas] son los renglones que dice haber leído:
 * sirven para comprobar que los demás datos realmente están en la etiqueta y no fueron inventados.
 */
data class LecturaNube(
    val nombre: String?,
    val bodega: String?,
    val cepa: String?,
    val anio: Int?,
    val lineas: List<String>,
)
