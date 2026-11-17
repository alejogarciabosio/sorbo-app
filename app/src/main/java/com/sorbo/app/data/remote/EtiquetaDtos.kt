// Capa de datos · DTOs de la función leer-etiqueta: el JSON exacto que espera y devuelve la Edge Function de Alejo.
package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.LecturaNube

/** La foto de la etiqueta en JPEG y base64 (la app ya la guarda reducida a 500 KB como máximo). */
data class PedidoEtiquetaDto(val imagen: String)

/** Los datos que la función no pudo leer vienen como null. */
data class RespuestaEtiquetaDto(
    val nombre: String? = null,
    val bodega: String? = null,
    val cepa: String? = null,
    val anio: Int? = null,
    val lineas: List<String>? = null,
) {
    fun aLectura() = LecturaNube(nombre, bodega, cepa, anio, lineas.orEmpty())
}
