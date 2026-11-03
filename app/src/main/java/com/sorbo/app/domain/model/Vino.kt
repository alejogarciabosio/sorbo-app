// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

import com.sorbo.app.domain.util.normalizar
import com.sorbo.app.domain.util.normalizarSinAnio
import java.time.Instant

/**
 * Un vino del diario. Cada añada es un vino distinto: "Rutini Malbec 2020" y "Rutini Malbec 2021"
 * tienen su propia ficha, sus degustaciones y su veredicto.
 */
data class Vino(
    val id: String,
    val nombre: String,
    val creadoEn: Instant,
    val actualizadoEn: Instant,
    val usuarioId: String? = null,
    val bodega: String? = null,
    val cepa: String? = null,
    val anio: Int? = null,
    val fotoPath: String? = null,
    val fotoUrl: String? = null,
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE,
    val eliminado: Boolean = false,
) {
    /** Nombre sin tildes, signos ni año. Sirve para reconocer si dos registros son el mismo vino. */
    val nombreNormalizado: String get() = normalizarSinAnio(nombre)

    val bodegaNormalizada: String get() = bodega?.let(::normalizar).orEmpty()

    /** Nombre, bodega, cepa y año juntos y normalizados: es lo que recorre el buscador. */
    val textoBusqueda: String
        get() = normalizar(listOfNotNull(nombre, bodega, cepa, anio?.toString()).joinToString(" "))

    /** Las bodegas están cargadas en los dos vinos y son distintas. */
    fun tieneOtraBodegaQue(otro: Vino): Boolean =
        bodegaNormalizada.isNotEmpty() && otro.bodegaNormalizada.isNotEmpty() && bodegaNormalizada != otro.bodegaNormalizada

    /** Las añadas están cargadas en los dos vinos y son distintas. */
    fun tieneOtraAnadaQue(otro: Vino): Boolean = anio != null && otro.anio != null && anio != otro.anio

    /** Mismo nombre y misma bodega (o sin bodega), pero de otra añada. */
    fun esOtraAnadaDe(otro: Vino): Boolean =
        !tieneOtraBodegaQue(otro) && tieneOtraAnadaQue(otro) && nombreNormalizado == otro.nombreNormalizado
}
